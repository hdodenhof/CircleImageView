/*
 * Copyright 2014 - 2017 Henning Dodenhof
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hdodenhof.circleimageview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;

public class CircleImageView extends ImageView implements SpringListener {

    public static final double EPSILON = 0.00001;

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLOR_DRAWABLE_DIMENSION = 2;

    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.WHITE;
    private static final int DEFAULT_CIRCLE_BACKGROUND_COLOR = Color.WHITE;
    private static final double DEFAULT_BORDER_WIDTH_IN_PERCENTAGE = 0.0f;
    private static final boolean DEFAULT_BORDER_OVERLAY = false;

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();
    private final Paint mCircleBackgroundPaint = new Paint();

    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;
    private int mCircleBackgroundColor = DEFAULT_CIRCLE_BACKGROUND_COLOR;

    private double mBorderWidthInPercentage = DEFAULT_BORDER_WIDTH_IN_PERCENTAGE;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private Bitmap mSelectedStateBitmap;

    private float mRadius;
    private float mBorderRadius;

    private ColorFilter mColorFilter;

    private boolean mReady;
    private boolean mSetupPending;
    private boolean mBorderOverlay;
    private boolean mDisableCircularTransformation;

    private int mPositionInTheAdapter;
    private boolean mUsePresetDrawable = false;
    private boolean mImplementsCustomBehaviour = false;

    // Animation
    private AnimatorUtil mAnimator;
    private Animation mAnimation;
    private Spring mSpring;

    // Constructors
    public CircleImageView(final Context context) {

        super(context);

        init();
    }

    public CircleImageView(final Context context, final AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public CircleImageView(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);

        mBorderColor = a.getColor(R.styleable.CircleImageView_civ_border_color, DEFAULT_BORDER_COLOR);
        mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_civ_border_width, DEFAULT_BORDER_WIDTH);
        mBorderOverlay = a.getBoolean(R.styleable.CircleImageView_civ_border_overlay, DEFAULT_BORDER_OVERLAY);

        final int borderWidthInPercentage = a.getInteger(R.styleable.CircleImageView_civ_border_width_in_percentage, 0);
        if (borderWidthInPercentage != 0) {
            mBorderWidthInPercentage = (borderWidthInPercentage / 100.0f);
        } else {
            mBorderWidthInPercentage = DEFAULT_BORDER_WIDTH_IN_PERCENTAGE;
        }

        if ((mBorderWidth != DEFAULT_BORDER_WIDTH) && (borderWidthIsUsingPercentage())) {
            throw new IllegalArgumentException("Both border_width and border_width_in_percentage have been set, only one is permitted");
        } else if ((mBorderWidth == DEFAULT_BORDER_WIDTH) && (!borderWidthIsUsingPercentage())) {
            throw new IllegalArgumentException("Border_width or border_width_in_percentage have not been set at all");
        }

        final Drawable selectedDrawable = a.getDrawable(R.styleable.CircleImageView_civ_border_selected_drawable);
        if (selectedDrawable != null) {
            mSelectedStateBitmap = ((BitmapDrawable) selectedDrawable).getBitmap();
        }

        mImplementsCustomBehaviour = a.getBoolean(R.styleable.CircleImageView_civ_implements_custom_behaviour, false);
        a.recycle();

        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mDisableCircularTransformation) {
            super.onDraw(canvas);
            return;
        }

        if (mBitmap == null) {
            return;
        }

        final float halfWidth = getWidth() / 2;
        final float halfHeight = getHeight() / 2;

        if (!mImplementsCustomBehaviour) {

            canvas.drawCircle(halfWidth, halfHeight, mRadius, mCircleBackgroundPaint);
            canvas.drawCircle(halfWidth, halfHeight, mRadius, mBitmapPaint);
            if (mBorderWidth > 0) {
                canvas.drawCircle(halfWidth, halfHeight, mBorderRadius, mBorderPaint);
            }
            return;
        }

        if (mUsePresetDrawable) {
            canvas.drawCircle(halfWidth, halfHeight, mRadius, mBitmapPaint);
        } else {
            mCircleBackgroundPaint.setColor(mCircleBackgroundColor);
            canvas.drawCircle(halfWidth, halfHeight, mRadius, mCircleBackgroundPaint);
            if (mBorderWidth > 0 && mCircleBackgroundPaint.getColor() == Color.WHITE) {
                canvas.drawCircle(halfWidth, halfHeight, mBorderRadius, mBorderPaint);
            }
        }

        if (isSelected()) {
            float cx = halfWidth - mSelectedStateBitmap.getWidth() / 2;
            float cy = halfHeight - mSelectedStateBitmap.getHeight() / 2;
            canvas.drawBitmap(mSelectedStateBitmap, cx, cy, null);
        }
    }

    @Override
    public ScaleType getScaleType() {

        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {

        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {

        if (adjustViewBounds) {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {

        super.setPadding(left, top, right, bottom);
        setup();
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {

        super.setPaddingRelative(start, top, end, bottom);
        setup();
    }

    // Properties
    @Override
    public void setImageBitmap(Bitmap bm) {

        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {

        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(int resId) {

        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    @Override
    public void setImageURI(Uri uri) {

        super.setImageURI(uri);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {

        if (cf == mColorFilter) {
            return;
        }

        mColorFilter = cf;
        mBitmapPaint.setColorFilter(mColorFilter);
        invalidate();
    }

    /**
     * Return the color drawn behind the circle-shaped drawable.
     *
     * @return The color drawn behind the drawable
     * @deprecated Use {@link #getCircleBackgroundColor()} instead.
     */
    @Deprecated
    public int getFillColor() {

        return getCircleBackgroundColor();
    }

    /**
     * Set a color to be drawn behind the circle-shaped drawable. Note that
     * this has no effect if the drawable is opaque or no drawable is set.
     *
     * @param fillColor The color to be drawn behind the drawable
     * @deprecated Use {@link #setCircleBackgroundColor(int)} instead.
     */
    @Deprecated
    public void setFillColor(@ColorInt int fillColor) {

        setCircleBackgroundColor(fillColor);
    }

    /**
     * Set a color to be drawn behind the circle-shaped drawable. Note that
     * this has no effect if the drawable is opaque or no drawable is set.
     *
     * @param fillColorRes The color resource to be resolved to a color and
     * drawn behind the drawable
     * @deprecated Use {@link #setCircleBackgroundColorResource(int)} instead.
     */
    @Deprecated
    public void setFillColorResource(@ColorRes int fillColorRes) {

        setCircleBackgroundColorResource(fillColorRes);
    }

    // Rebound
    @Override
    public void onSpringUpdate(Spring spring) {

        float value = (float) spring.getCurrentValue();
        float scale = 1f - (value * 0.5f);

        setScaleX(scale);
        setScaleY(scale);
    }

    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }

    public void setBackgroundColorHex(String colorHex) {

        if (colorHex != null) {
            mUsePresetDrawable = false;
            mCircleBackgroundColor = Color.parseColor("#" + colorHex);
        } else {
            mUsePresetDrawable = true;
        }

        invalidate();
    }

    public boolean isDisableCircularTransformation() {

        return mDisableCircularTransformation;
    }

    public void setDisableCircularTransformation(boolean disableCircularTransformation) {

        if (mDisableCircularTransformation == disableCircularTransformation) {
            return;
        }

        mDisableCircularTransformation = disableCircularTransformation;
        initializeBitmap();
    }

    public int getPositionInTheAdapter() {

        return mPositionInTheAdapter;
    }

    public void setPositionInTheAdapter(int position) {

        mPositionInTheAdapter = position;
    }

    public int getBorderColor() {

        return mBorderColor;
    }

    public void setBorderColor(@ColorRes int borderColor) {

        mBorderColor = ContextCompat.getColor(getContext(), borderColor);
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public boolean isBorderOverlay() {

        return mBorderOverlay;
    }

    public void setBorderOverlay(boolean borderOverlay) {

        if (borderOverlay == mBorderOverlay) {
            return;
        }

        mBorderOverlay = borderOverlay;
        setup();
    }

    public int getCircleBackgroundColor() {

        return mCircleBackgroundColor;
    }

    public void setCircleBackgroundColor(@ColorInt int circleBackgroundColor) {

        if (circleBackgroundColor == mCircleBackgroundColor) {
            return;
        }

        mCircleBackgroundColor = circleBackgroundColor;
        mCircleBackgroundPaint.setColor(circleBackgroundColor);
        invalidate();
    }

    public void setCircleBackgroundColorResource(@ColorRes int circleBackgroundRes) {

        setCircleBackgroundColor(getContext().getResources().getColor(circleBackgroundRes));
    }

    public void animateView(float animateValue, boolean setEndValue, Context context) {

        if (mAnimator == null) {
            mAnimator = new AnimatorUtil();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mSpring == null) {
                mSpring = mAnimator.getNewSpring(AnimatorUtil.REBOUND_TENSION, AnimatorUtil.REBOUND_DAMPER);
                mSpring.addListener(this);
            }

            if (setEndValue) {
                mSpring.setEndValue(animateValue);
                return;
            }

            mSpring.setCurrentValue(animateValue);

            return;
        }

        if (mAnimation == null) {
            mAnimation = mAnimator.getPressBouncingAnimation((Activity) context);
        }

        startAnimation(mAnimation);
    }

    // Private
    private Bitmap getBitmapFromDrawable(Drawable drawable) {

        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLOR_DRAWABLE_DIMENSION, COLOR_DRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private void initializeBitmap() {

        if (mDisableCircularTransformation) {
            mBitmap = null;
        } else {
            mBitmap = getBitmapFromDrawable(getDrawable());
        }
        setup();
    }

    private void setup() {

        if (!mReady) {
            mSetupPending = true;
            return;
        }

        if (mBitmap == null) {
            return;
        }

        final RectF controlRectF = new RectF(0, 0, getWidth(), getHeight());
        mRadius = Math.min(controlRectF.height() / 2, controlRectF.width() / 2);
        if (borderWidthIsUsingPercentage()) {
            mBorderWidth = (int) (mRadius * mBorderWidthInPercentage);
        }
        mBorderRadius = Math.min((controlRectF.height() - mBorderWidth) / 2, (controlRectF.width() - mBorderWidth) / 2);

        // TODO: neutralize the points where it manipulates the passed drawable in order to draw it (all cases except multicolor filter)
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mCircleBackgroundPaint.setStyle(Paint.Style.FILL);
        mCircleBackgroundPaint.setAntiAlias(true);
        mCircleBackgroundPaint.setColor(Color.WHITE);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        updateShaderMatrix(controlRectF, mBitmap.getWidth(), mBitmap.getHeight());
        invalidate();
    }

    private void updateShaderMatrix(RectF rectF, int bitmapWidth, int bitmapHeight) {

        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (bitmapWidth * rectF.height() > rectF.width() * bitmapHeight) {
            scale = rectF.height() / (float) bitmapHeight;
            dx = (rectF.width() - bitmapWidth * scale) * 0.5f;
        } else {
            scale = rectF.width() / (float) bitmapWidth;
            dy = (rectF.height() - bitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    private boolean borderWidthIsUsingPercentage() {

        return (Math.abs(mBorderWidthInPercentage - DEFAULT_BORDER_WIDTH_IN_PERCENTAGE) >= EPSILON);
    }

    private void init() {

        super.setScaleType(SCALE_TYPE);
        mReady = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new OutlineProvider());
        }

        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class OutlineProvider extends ViewOutlineProvider {

        @Override
        public void getOutline(View view, Outline outline) {

            final RectF controlRectF = new RectF(0, 0, getWidth(), getHeight());
            Rect bounds = new Rect();
            controlRectF.roundOut(bounds);
            outline.setRoundRect(bounds, bounds.width() / 2.0f);
        }
    }
}
