package de.hdodenhof.circleimageview.sample;

import android.app.Activity;
import android.os.Bundle;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CircleImageView i1 = (CircleImageView) findViewById(R.id.img_blur);
        CircleImageView i2 = (CircleImageView) findViewById(R.id.img_blur_2);
        CircleImageView i3 = (CircleImageView) findViewById(R.id.img_blur_3);

        Picasso.with(this).load("http://wallpapersbq.com/images/dr-house/dr-house-wallpaper-14.jpg")
                .into(i2);

        Picasso.with(this).load("http://www.pecsma.hu/wp-content/uploads/2014/04/hugh-laurie-dr-house-163437.jpg")
                .into(i3);

        i3.setBlurRadius(25);
    }

}
