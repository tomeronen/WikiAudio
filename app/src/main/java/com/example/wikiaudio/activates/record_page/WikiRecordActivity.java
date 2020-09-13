package com.example.wikiaudio.activates.record_page;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.example.wikiaudio.R;
import com.example.wikiaudio.audio_recoder.VoiceRecorder;
import com.example.wikiaudio.file_manager.FileManager;
import com.example.wikiaudio.wikipedia.WikiPage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

public class WikiRecordActivity extends AppCompatActivity {

    private static final int REQ_RECORD_PERMISSION = 12;
    public static final String WIKI_PAGE_TAG = "wikiPageTag" ;
    WebView wikiPageView;
    ProgressBar progressBar;
    FloatingActionButton recordButton;
    AppCompatActivity activity;
    WikiPage wikiPage;
    FileManager fileManager;
    private GestureDetectorCompat gestureDetectorCompat = null;
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
        fileManager = new FileManager(this);
        Gson gson = new Gson();
        wikiPage = gson.fromJson(getIntent()
                                                .getStringExtra(WIKI_PAGE_TAG), WikiPage.class);
        progressBar.setMax(wikiPage.numberOfSections() - 1); // '-1' - we start at zero.
//        wikiPageView.loadData(wikiPage.getSection(curSection)
//                                            ,"text/html", "UTF-8");
        activity = this;
        setOnRecordButton();
//        setSwipeDetector();
        }

    private void setSwipeDetector() {
        SwipeFunctions sf = new SwipeFunctions() {
            @Override
            public void onRightSwipe() {
                if(curSection > 0)
                {
                    --curSection;
//                    wikiPageView.loadData(wikiPage.getSection(curSection)  ,
//                            "text/html", "UTF-8");
                    progressBar.setProgress(curSection);
                }
            }

            @Override
            public void onLeftSwipe() {
                if(curSection + 1 < wikiPage.numberOfSections())
                {
                    ++curSection;
//                    wikiPageView.loadData(wikiPage.getSection(curSection)  ,
//                            "text/html", "UTF-8");
                    progressBar.setProgress(curSection);
                }
            }
        };
        DetectSwipeGestureListener gestureListener
                = new DetectSwipeGestureListener(sf);
        this.gestureDetectorCompat = new GestureDetectorCompat(this, gestureListener);
        this.wikiPageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetectorCompat.onTouchEvent(event);
                return wikiPageView.onTouchEvent(event);
            }
        });
    }

    private void setOnRecordButton() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean havePermissions = checkRecordingPermissions();
                String fp = fileManager.getFilePath(wikiPage.getTitle(), curSection);
                Log.d("file", fp);
                File f = new File(fp);
                Log.d("file path:", f.getAbsolutePath());
                Log.d("file exists:", Boolean.toString(f.exists()));
                VoiceRecorder voiceRecorder  = new VoiceRecorder(fp, activity);

                if (startRecording && havePermissions) {
                        startRecording = false;
                        startBlinkingAnimation(recordButton);
                        voiceRecorder.startRecording();
//                      recorder.setOutputFile(fp);
//                      recorder.prepare();
//                      recorder.start();
                } else if (!havePermissions) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQ_RECORD_PERMISSION);
                } else {
                    startRecording = true;
                    stopBlinkingAnimation(recordButton);
                    try {
                        voiceRecorder.stopRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }

    private boolean checkRecordingPermissions() {
        int writeToStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readFromStoragePermission =  ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int recordPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        return writeToStoragePermission == PackageManager.PERMISSION_GRANTED &&
                recordPermission == PackageManager.PERMISSION_GRANTED &&
                readFromStoragePermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_RECORD_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                        // TODO - START RECORDING OUR MAKE PRESS AGAIN?

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)) {
                        // TODO explain why we need.


                    }
                }
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

//    in case we want to make a full browser:
//    private class MyBrowser extends WebViewClient {
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            view.loadUrl(url);
//            return true;
//        }
//    }


}
