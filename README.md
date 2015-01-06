CircleImageView
===============

A fast circular ImageView perfect for profile images. This is based on [RoundedImageView from Vince Mi](https://github.com/vinc3m1/RoundedImageView) which itself is based on [techniques recommended by Romain Guy](http://www.curious-creature.org/2012/12/11/android-recipe-1-image-with-rounded-corners/).

![CircleImageView](https://raw.github.com/hdodenhof/CircleImageView/master/screenshot.png)

It uses a BitmapShader and **does not**:
* create a copy of the original bitmap
* use a clipPath (which is neither hardware accelerated nor anti-aliased)
* use setXfermode to clip the bitmap (which means drawing twice to the canvas)

As this is just a custom ImageView and not a custom Drawable or a combination of both, it can be used with all kinds of drawables, i.e. a PicassoDrawable from [Picasso](https://github.com/square/picasso) or other non-standard drawables (needs some testing though).

Gradle
------
```
dependencies {
    ...
    compile 'de.hdodenhof:circleimageview:1.2.1'
}
```

Usage
-----
```xml
<de.hdodenhof.circleimageview.CircleImageView
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/profile_image"
    android:layout_width="96dp"
    android:layout_height="96dp"
    android:src="@drawable/profile"
    app:border_width="2dp"
    app:border_color="#FF000000"/>
```
![CircleImageView](https://raw.githubusercontent.com/SimoneBellotti/CircleImageView/master/screenshot.gif)

Attributes
----------

* ```app:border_width``` (dimension)
* ```app:border_color``` (color)
* ```app:selector_color``` (color)
* ```app:selector_border_color``` (color)

Limitations
-----------
* The ScaleType is always CENTER_CROP and you'll get an exception if you try to change it. This is (currently) by design as it's perfectly fine for profile images.
* Enabling `adjustViewBounds` is not supported as this requires an unsupported ScaleType
* If you use Picasso for fetching images, you need to set the `noFade()` option to avoid messed up images. If you want to keep the fadeIn animation, you have to fetch the image into a `Target` and apply a custom animation when setting it as source for the `CircleImageView` in `onBitmapLoaded()`.
* Using a `TransitionDrawable` with `CircleImageView` doesn't work properly and leads to messed up images.

Changelog
---------
* **1.2.1**
    * Fix ColorDrawables not being rendered properly on Lollipop
* **1.2.0**
    * Add support for setImageURI(Uri uri)
    * Fix view not being initialized when using CircleImageView(Context context)
* **1.1.1**
    * Fix border being shown although border width is set to 0
* **1.1.0**
    * Add support for ColorDrawables
    * Add getters and setters for border color and border width
* **1.0.1**
    * Prevent crash due to OutOfMemoryError
* **1.0.0**
    * Initial release

License
-------

    Copyright 2014 Henning Dodenhof

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
