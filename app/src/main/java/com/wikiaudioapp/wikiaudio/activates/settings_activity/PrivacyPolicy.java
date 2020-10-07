package com.wikiaudioapp.wikiaudio.activates.settings_activity;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.wikiaudioapp.wikiaudio.R;

public class PrivacyPolicy extends AppCompatActivity {

    private static final String PRIVACY_POLICY_URL = "https://sites.google.com/view/wikiaudio/home";
    private WebView privacyPolicyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        privacyPolicyView = findViewById(R.id.privacyPolicyView);
        privacyPolicyView.loadUrl(PRIVACY_POLICY_URL);
    }
}