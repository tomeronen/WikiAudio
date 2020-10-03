package com.example.wikiaudio.activates.record_page;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.R;

public class RecordingTipsActivity extends AppCompatActivity {

    private TextView tipsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_tips);
        tipsText = findViewById(R.id.tipsTextView);
        tipsText.setText(R.string.tips_and_guide_lines);
        tipsText.setMovementMethod(new ScrollingMovementMethod());
    }
}