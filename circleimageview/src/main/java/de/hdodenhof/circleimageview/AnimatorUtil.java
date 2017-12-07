package de.hdodenhof.circleimageview;

import android.app.Activity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

public class AnimatorUtil {

    // Constants
    public static final int DEFAULT_ANIMATION_DURATION_SHORT = 200;
    public static final int DEFAULT_ANIMATION_DURATION_MEDIUM = 300;
    private static final int SEARCH_ROTATION_ANIMATION_DURATION = 400;

    public static final double REBOUND_TENSION = 800;
    public static final double REBOUND_DAMPER = 20;

    public Animation getPressBouncingAnimation(Activity activity) {

        return AnimationUtils.loadAnimation(activity, R.anim.button_bounce_effect);
    }

    public Spring getNewSpring(final double tension, final double damper) {

        SpringSystem springSystem = SpringSystem.create();
        Spring spring = springSystem.createSpring();
        SpringConfig config = new SpringConfig(tension, damper);
        spring.setSpringConfig(config);

        return spring;
    }
}
