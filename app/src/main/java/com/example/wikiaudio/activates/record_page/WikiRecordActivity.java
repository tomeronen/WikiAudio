package com.example.wikiaudio.activates.record_page;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import com.example.wikiaudio.R;
import com.example.wikiaudio.audio_recoder.VoiceRecorderOptionB;
import com.example.wikiaudio.file_manager.FileManager;
import com.example.wikiaudio.wikipedia.Wikipage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

public class WikiRecordActivity extends AppCompatActivity {

    private static final int REQ_RECORD_PERMISSION = 12;
    public static final String WIKI_PAGE_TAG = "WikipageTag" ;
    TextView WikipageView;
    ProgressBar progressBar;
    FloatingActionButton recordButton;
    AppCompatActivity activity;
    Wikipage Wikipage;
    FileManager fileManager;
    private GestureDetectorCompat gestureDetectorCompat = null;
    private VoiceRecorderOptionB recorder;
    boolean startRecording = true; // when button pressed record our stop recording?
    int curSection = 0;
    int curParagraph = 0;
    String format;
    Button nextButton;
    Button previousButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_recored_acrivaty);
        WikipageView = findViewById(R.id.Wikipage);
        progressBar = findViewById(R.id.progressBar);
        recordButton = findViewById(R.id.recoredButton);
        fileManager = new FileManager(this);
        recorder = new VoiceRecorderOptionB();
        format  = recorder.format;
        Gson gson = new Gson();
        Wikipage = gson.fromJson(getIntent()
                .getStringExtra(WIKI_PAGE_TAG), Wikipage.class);
        int paragraphAmount = 0;
        for(Wikipage.Section section: Wikipage.getSections())
        {
            paragraphAmount += section.getContents().size();

        }
        progressBar.setMax(Wikipage.numberOfSections() + paragraphAmount +  - 1); // '-1' - we start at zero.
        loadWikiDataToView(curSection, curParagraph);
        activity = this;
        setOnRecordButton();

        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Swipe Detector", "left");
                ++curParagraph;
                if(curParagraph > Wikipage.getSection(curSection).getContents().size())
                { // got to end of section
                    curParagraph = 0;
                    curSection++;
                }
                loadWikiDataToView(curSection, curParagraph);
                progressBar.setProgress(curSection);
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Swipe Detector", "right");
                --curParagraph;
                if(curParagraph < 0)
                { // got to end of section
                    if(curSection > 0)
                    {
                        curSection--;
                        curParagraph = Wikipage.getSection(curSection).getContents().size();
                    }
                    else
                    {
                        curParagraph = 0;
                    }
                }
                loadWikiDataToView(curSection, curParagraph);
                progressBar.setProgress(curSection);
            }
        });

        // todo there is a problem with swipe
//        setSwipeDetector();
    }

    private void setSwipeDetector() {
        SwipeFunctions sf = new SwipeFunctions() {
            @Override
            public void onRightSwipe() {
                if(curSection > 0)
                {
                    Log.d("Swipe Detector", "right");
                    --curSection;
                    loadWikiDataToView(curSection, curParagraph);
                    progressBar.setProgress(curSection);
                }
            }

            @Override
            public void onLeftSwipe() {
                if(curSection + 1 < Wikipage.numberOfSections())
                {
                    Log.d("Swipe Detector", "left");
                    ++curSection;
                    loadWikiDataToView(curSection, curParagraph);
                    progressBar.setProgress(curSection);
                }
            }
        };
        DetectSwipeGestureListener gestureListener
                = new DetectSwipeGestureListener(sf);


        this.gestureDetectorCompat = new GestureDetectorCompat(this, gestureListener);
        this.WikipageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("touch", "was touched");
                gestureDetectorCompat.onTouchEvent(event);
                return WikipageView.onTouchEvent(event);
            }
        });
    }

    private void loadWikiDataToView(int curSection, int curParagraph) {
        if(curParagraph == 0)
        {
            WikipageView.setText(Wikipage.getSection(curSection).getTitle());
        }
        else
        {
            WikipageView.setText(
                    Wikipage.getSection(curSection).getContents().get(curParagraph - 1));
        }
    }

    private void setOnRecordButton() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean havePermissions = checkRecordingPermissions();
//                VoiceRecorder voiceRecorder  = new VoiceRecorder(fp, activity);
                if (startRecording && havePermissions) {
                    startRecording = false;
                    startBlinkingAnimation(recordButton);
                    String fp = fileManager.getFilePath(Wikipage.getTitle(),
                            curSection,
                            curParagraph)
                            + "." + recorder.format;
                    recorder.startRecording(fp);
                } else if (!havePermissions) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQ_RECORD_PERMISSION);
                } else {
                    startRecording = true;
                    stopBlinkingAnimation(recordButton);
                    recorder.stopRecording();
                    Wikipedia wikipedia = new Wikipedia(activity);

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