package de.hdodenhof.circleimageview.sample;

import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView iv = findViewById(R.id.iv2);

        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TransitionDrawable) iv.getDrawable()).startTransition(500);
            }
        }, 2000);
    }

}
