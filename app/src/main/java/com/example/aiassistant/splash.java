package com.example.aiassistant;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.app.StatusBarManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class splash extends AppCompatActivity {

    TextView tv;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        iv  = (ImageView) findViewById(R.id.imageView);

        Animation myanim = AnimationUtils.loadAnimation(this,R.anim.my_animation);
        iv.startAnimation(myanim);

        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    sleep(4000);
                    Intent i  =new Intent(splash.this,MainActivity.class);
                    startActivity(i);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        myThread.start();



    }
}