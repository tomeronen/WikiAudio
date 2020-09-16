package com.example.wikiaudio.activates.loading;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.activates.MainActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent main = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(main);
                finish();
            }
        }, 2500);
    }
}