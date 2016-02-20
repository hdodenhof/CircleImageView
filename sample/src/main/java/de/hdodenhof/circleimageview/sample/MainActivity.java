package de.hdodenhof.circleimageview.sample;

import android.app.Activity;
import android.os.Bundle;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CircleImageView imageView = (CircleImageView) findViewById(R.id.circleimage);
        imageView.setTint(R.color.blue);
    }

}
