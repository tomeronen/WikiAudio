package com.example.wikiaudio.activates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.wikiaudio.R;
import com.example.wikiaudio.audio_player.AudioPlayer;
import com.example.wikiaudio.audio_recoder.AudioRecorder;
import com.example.wikiaudio.wikipedia.WikiPage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

public class WikiRecordActivity extends AppCompatActivity {

    private static final int REQ_RECORD_PERMISSION = 12;
    private static final String WIKI_PAGE_TAG = "wikiPageTag" ;
    WebView wikiPageView;
    ProgressBar progressBar;
    FloatingActionButton recordButton;
    AppCompatActivity activity;
    WikiPage wikiPage;
    private MediaRecorder recorder;
    boolean startRecording = true; // when button pressed record our stop recording?
    String DEBUG_URL = "https://en.wikipedia.org/wiki/Android_(operating_system)";
    int curSection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_recored_acrivaty);
        wikiPageView = findViewById(R.id.wikiPage);
        progressBar = findViewById(R.id.progressBar);
        recordButton = findViewById(R.id.recoredButton);
        Gson gson = new Gson();
        wikiPage = gson.fromJson(getIntent()
                                                .getStringExtra(WIKI_PAGE_TAG), WikiPage.class);
        progressBar.setMax(wikiPage.numberOfSections());
        wikiPageView.loadData(wikiPage.getSection(curSection)
                                            ,"text/html", "UTF-8");
        activity = this;
        setOnRecordButton();
    }

    private void setOnRecordButton() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean havePermissions = checkRecordingPermissions();
                if (startRecording && havePermissions) {

                    try {
                        // TODO TAKE OUT TO SPECIAL CLASS
                        startRecording = false;
                        startBlinkingAnimation(recordButton);
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        String a =  activity.getFilesDir() + "/" + wikiPage.getTitle() + ".mp3";
                        recorder.setOutputFile(a);
                        recorder.prepare();
                        recorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (!havePermissions) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQ_RECORD_PERMISSION);
                } else {
                    progressBar.incrementProgressBy(1); //todo for debug only
                    startRecording = true;
                    stopBlinkingAnimation(recordButton);
                    if(recorder != null)
                    {
                        recorder.stop();
                        try {
                            MediaPlayer mp = new MediaPlayer();
                            String a =  activity.getFilesDir() + "/" + wikiPage.getTitle() + ".mp3";
                            mp.setDataSource(a);
                            mp.prepare();
                            mp.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });
    }

    private boolean checkRecordingPermissions() {
        int writeToStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int recordPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        return writeToStoragePermission == PackageManager.PERMISSION_GRANTED &&
                recordPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_RECORD_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        // TODO - START RECORDING OUR MAKE PRESS AGAIN?

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)) {
                        // TODO explain why we need.


                    }
                }
        }
    }


    //
    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private void startBlinkingAnimation(View v){
        Animation mAnimation = new AlphaAnimation(1, 0);
        mAnimation.setDuration(600);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        v.startAnimation(mAnimation);
    }

    private void stopBlinkingAnimation(View v){
        v.clearAnimation();
    }

//    private String getFilename()
//    {
//        String filepath = Environment.getExternalStorageDirectory().getPath();
//        File file = new File(filepath,getFilesDir());
//        if(!file.exists()){
//            file.mkdirs();
//        }
//        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp3");
//    }

}
