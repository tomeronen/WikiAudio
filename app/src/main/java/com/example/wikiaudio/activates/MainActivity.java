package com.example.wikiaudio.activates;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.location.LocationTracker;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.google.android.gms.location.LocationCallback;

public class MainActivity extends AppCompatActivity {

    Wikipedia wikipedia;
    LocationTracker locationTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wikipedia = new Wikipedia(this);
        locationTracker = new LocationTracker(this);
    }
}
