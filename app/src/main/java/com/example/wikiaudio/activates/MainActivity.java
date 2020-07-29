package com.example.wikiaudio.activates;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.Wikipedia;

public class MainActivity extends AppCompatActivity {

    Wikipedia wikipedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wikipedia = new Wikipedia(this);
    }
}
