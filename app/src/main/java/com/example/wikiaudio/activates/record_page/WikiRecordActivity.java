package com.example.wikiaudio.activates.record_page;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentManager;

import com.example.wikiaudio.R;
import com.example.wikiaudio.audio_recoder.ARecorder;
import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.file_manager.FileManager;
import com.example.wikiaudio.wikipedia.server.WorkerListener;
import com.example.wikiaudio.wikipedia.wikipage.PageAttributes;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class WikiRecordActivity extends AppCompatActivity {

    private static final int REQ_RECORD_PERMISSION = 12;
    public static final String WIKI_PAGE_TAG = "WikipageTag" ;
    WebView sectionView;
    ProgressBar progressBar;
    ProgressBar loadingContent;
    FloatingActionButton recordButton;
    AppCompatActivity activity;
    Wikipage wikipage = new Wikipage();
    FileManager fileManager;
    private GestureDetectorCompat gestureDetectorCompat = null;
    private ARecorder recorder;
    boolean startRecording = true; // when button pressed record our stop recording?
    int curSection = 0;
    String format;
    Button nextButton;
    Button previousButton;
    private TextView progressCounter;
    private int numberOfScreens;
    private int screenCounter;
    private TextView sectionTitle;
    private FloatingActionButton uploadButton;
    private String introMsg = ", from Wikipedia, the free encyclopedia," +
            " at E N dot wikipedia dot org.";
    private ImageButton playButton;
    private MediaPlayer mediaPlayer;
    private boolean currentlyPlaying = false;
    private FloatingActionButton showSectionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_recored_acrivaty);
        initVars();
        loadContent();
        setOnClickButtons();
        showDialog();

        // todo there is a problem with swipe
//        setSwipeDetector();
    }



    private void loadContent() {
        loadingContent.setVisibility(View.VISIBLE);
        String wikipageTitle = getIntent()
                .getStringExtra(WIKI_PAGE_TAG);
        List<PageAttributes> pageAttributesList = new ArrayList<>();
        pageAttributesList.add(PageAttributes.title);
        pageAttributesList.add(PageAttributes.content);
        Holder.wikipedia.getWikipage(wikipageTitle, pageAttributesList, wikipage,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        if(wikipage.getSections() != null) {
                             numberOfScreens = 2 + wikipage.getSections().size();
                            progressBar.setMax(numberOfScreens);
                            progressCounter.setText(String.format("%d/%d",screenCounter,
                                    numberOfScreens));
                            loadWikiDataToView(curSection);
                        }
                        loadingContent.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure() {

                    }
                });
    }

    private void initVars() {
        sectionView = findViewById(R.id.section_view);
        progressBar = findViewById(R.id.progressBar);
        recordButton = findViewById(R.id.recoredButton);
        loadingContent = findViewById(R.id.loadingContent);
        fileManager = new FileManager(this);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        playButton = findViewById(R.id.playSection);
        activity = this;
        recorder = new ARecorder();
        format  = recorder.format;
        progressCounter = findViewById(R.id.progressBarCounter);
        sectionTitle = findViewById(R.id.sectionTitleView);
        uploadButton = findViewById(R.id.uploadButton);
        showSectionsButton = findViewById(R.id.showSectionsButton);
        screenCounter = 0;

        //WebView
        sectionView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

//                 Inject CSS when page is done loading
                setCss();
                super.onPageFinished(view, url);
            }

        });
        sectionView.getSettings().setJavaScriptEnabled(true);
        sectionView.getSettings().setDomStorageEnabled(true);
        sectionView.getSettings().setLoadsImagesAutomatically(true);
    }

    private void setSwipeDetector() {
        SwipeFunctions sf = new SwipeFunctions() {
            @Override
            public void onRightSwipe() {
                if(curSection > 0)
                {
                    Log.d("Swipe Detector", "right");
                    --curSection;
                    loadWikiDataToView(curSection);
                    progressBar.setProgress(curSection);
                }
            }

            @Override
            public void onLeftSwipe() {
                if(curSection + 1 < wikipage.numberOfSections())
                {
                    Log.d("Swipe Detector", "left");
                    ++curSection;
                    loadWikiDataToView(curSection);
                    progressBar.setProgress(curSection);
                }
            }
        };
        DetectSwipeGestureListener gestureListener
                = new DetectSwipeGestureListener(sf);


        this.gestureDetectorCompat = new GestureDetectorCompat(this, gestureListener);
        this.sectionView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("touch", "was touched");
                gestureDetectorCompat.onTouchEvent(event);
                return sectionView.onTouchEvent(event);
            }
        });
    }

    private void updateProgressCounter() {
        String counterText = String.valueOf(screenCounter + "/" + numberOfScreens);
        progressCounter.setText(counterText);
    }

    private void loadWikiDataToView(int curSection) {
        if(curSection == 0)
        {
            sectionView.loadData(getString(R.string.license_string),
                    "text/html", "utf-8");
            return;
        }
        if(curSection == 1)
        {
            sectionView.loadData(wikipage.getTitle() + introMsg,
                    "text/html", "utf-8");
            return;
        }
        curSection = curSection - 2; // we finished showing the intros. go back to zero.
        if(curSection < wikipage.getSections().size())
        {
                sectionTitle.setText(wikipage.getSection(curSection).getTitle());
            String htmlText = wikipage.getSection(curSection).getContents();
            sectionView.loadDataWithBaseURL("https://en.wikipedia.org",
                    htmlText,
                    "text/html",
                    "UTF-8",
                    null);
        }
    }

    private void setOnClickButtons() {
        setOnClickRecord();
        setOnClickNext();
        setOnClickPrevious();
        setOnClickUpload();
        setOnClickPlay();
        setOnShowSectionsClick();
    }

    private void setOnShowSectionsClick() {
        showSectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                List<String> sectionsNames = new ArrayList<>();
                sectionsNames.add("section One");
                sectionsNames.add("section Two");
                sectionsNames.add("section Three");
                SectionsDialog introDialog = new SectionsDialog(sectionsNames);
                introDialog.show(fragmentManager, "introTag");
            }
        });
    }

    private void setOnClickPlay() {
        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!startRecording) // we are recording. stop, and then play.
                {

                }
//                recorder.stopRecording();
                try {
                    if(currentlyPlaying)
                    {
                        if(mediaPlayer != null)
                        {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                        }
                        currentlyPlaying = false;
                    }
                    else
                    {
                        mediaPlayer = new MediaPlayer();
                        String localPathToFile =
                                fileManager.getFilePath(wikipage.getTitle(),
                                        curSection)  + "." + recorder.format;
                        mediaPlayer.setDataSource(localPathToFile);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        currentlyPlaying = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(activity, "there was a problem with playing this section",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void setOnClickUpload() {
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                SimpleAlertDialogFragment introDialog = new SimpleAlertDialogFragment(
                        "this action will upload your recording to wikipedia." +
                                "are you sure you finished you recording? ",
                        "yes. upload recording.",
                        "no i want to continue working",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for(int i = 0;  i < numberOfScreens; i++)
                                {
                                    String localPathToFile =
                                            fileManager.getFilePath(wikipage.getTitle(),
                                            curSection)  + "." + recorder.format;
                                    Holder.wikipedia.uploadFile(wikipage.getTitle(),
                                            localPathToFile);
                                }
                                Toast.makeText(activity, "thank you! we are uploading your," +
                                        " recording and it will be available on the app." +
                                        " this might take some time. ", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }
                );
                introDialog.show(fragmentManager, "introTag");
            }
        });
    }

    private void setOnClickPrevious() {
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Swipe Detector", "right");
                if(curSection > 0)
                {
                    curSection--;
                    loadWikiDataToView(curSection);
                    screenCounter--;
                    progressBar.setProgress(screenCounter);
                    updateProgressCounter();
                }
            }
        });
    }

    private void setOnClickNext() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Swipe Detector", "left");
                if(wikipage.getSections() != null &&
                        curSection + 1 < wikipage.getSections().size())
                {
                    ++curSection;
                    loadWikiDataToView(curSection);
                    screenCounter++;
                    progressBar.setProgress(screenCounter);
                    updateProgressCounter();
                }
            }
        });
    }

    private void setOnClickRecord() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean havePermissions = checkRecordingPermissions();
//                VoiceRecorder voiceRecorder  = new VoiceRecorder(fp, activity);
                if (startRecording && havePermissions) {
                    startRecording = false;
                    startBlinkingAnimation(recordButton);
                    String fp = fileManager.getFilePath(wikipage.getTitle(),
                            curSection)
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

    private void showDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SimpleAlertDialogFragment introDialog = new SimpleAlertDialogFragment(
                getString(R.string.tips_and_guide_lines),
                getString(R.string.tips_button),
                getString(R.string.dont_show_tips),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent tipsAngGuideLineIntent = new Intent(activity,
                                RecordingTipsActivity.class);
                        startActivity(tipsAngGuideLineIntent);
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }
        );
        introDialog.show(fragmentManager, "introTag");
    }

    private void stopBlinkingAnimation(View v){
        v.clearAnimation();
    }

    private void setCss() {
            try {
                InputStream inputStream = getAssets().open("wikipediaCss.css");
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();
                String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
//                String cssString = getString(R.string.wikiCssStyle);
                sectionView.loadUrl("javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        // Tell the browser to BASE64-decode the string into your script !!!
                        "style.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(style)" +
                        "})()");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}