package com.example.wikiaudio.activates.record_page;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.audio_recoder.ARecorder;
import com.example.wikiaudio.data.AppData;
import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.file_manager.FileManager;
import com.example.wikiaudio.wikipedia.server.WorkerListener;
import com.example.wikiaudio.wikipedia.wikipage.PageAttributes;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WikiRecordActivity extends AppCompatActivity {

    private static final int REQ_RECORD_PERMISSION = 12;
    public static final String WIKI_PAGE_TAG = "WikipageTag" ;
    private static final String CUR_SECTION_TAG = "CUR_SECTION_TAG";
    WebView sectionView;
    AppCompatActivity activity;
    Wikipage wikipage = new Wikipage();
    FileManager fileManager;
    private GestureDetectorCompat gestureDetectorCompat = null;
    int curSection = 0;
    String format;
    private int numberOfScreens;
    private int screenCounter;
    private TextView sectionTitle;
    private String introMsg = ", from Wikipedia, the free encyclopedia," +
            " at E N dot wikipedia dot org.";
    private MediaPlayer mediaPlayer;
    private boolean currentlyPlaying = false;
    private AppData appData;

    // recording:
    private List<SectionRecordingData> recordingDataList;
    private ARecorder recorder;
    boolean startRecording = true; // when button pressed record our stop recording

    // Buttons:
    private FloatingActionButton showSectionsButton;
    private ImageButton playButton;
    private FloatingActionButton recordButton;
    private Button nextButton;
    private FloatingActionButton pauseButton;
    private FloatingActionButton stopButton;
    private FloatingActionButton deleteRecording;
    private Button previousButton;
    private FloatingActionButton uploadButton;


    // recording Timer:
    CountDownTimer recordingTimer;
    TextView recordingTimerView;
    private long milSeconds;

    // progress bars:
    ProgressBar progressBar;
    ProgressBar loadingContent;
    private TextView progressCounter;
    private boolean recordingPaused = false;
    private boolean recodedInitialized = false;


    // unwanted sections for recording
    static HashSet<String> unwantedSectionsForRecording;
    static { //  these are sections we don't want our users to record:
        unwantedSectionsForRecording = new HashSet<>();
        unwantedSectionsForRecording.add("See also");
        unwantedSectionsForRecording.add("References");
        unwantedSectionsForRecording.add("External links");
        unwantedSectionsForRecording.add("Notes");
        unwantedSectionsForRecording.add("Bibliography");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_recored_acrivaty);
        initVars();
        loadContent();
        setOnClickButtons();
        if(savedInstanceState == null)
        { // first instance
            showDialog();
        }
        else
        {
            curSection = savedInstanceState.getInt(CUR_SECTION_TAG);
        }

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
                            cleanUnwantedSections(wikipage.getSections());

                            // add default intro sections:
                            addDefaultSections();

                            // init counters:
                            numberOfScreens = wikipage.getSections().size();
                            initRecordingData();

                            // initialize progress counters:
                            progressCounter.setText(String.format("%d/%d",screenCounter,
                                    numberOfScreens));
                            progressBar.setMax(numberOfScreens);

                            updateUI(curSection);
                        }
                        loadingContent.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure() {

                    }
                });
    }

    private void addDefaultSections() {
        if(wikipage != null
                && wikipage.getSections() != null
                && !wikipage.getSection(0).getTitle().equals("Licensing")) // there is no intro
        {
            wikipage.getSections().add(0,
                    new Wikipage.Section("Intro",
                    wikipage.getTitle() + introMsg));
            wikipage.getSections().add(0,
                    new Wikipage.Section("Licensing",
                    getResources().getString(R.string.license_string)));
        }
    }

    private void cleanUnwantedSections(List<Wikipage.Section> sections) {
        List<Wikipage.Section> unwantedSections = new ArrayList<>();
        for(Wikipage.Section section: sections)
        {
            if(section.getTitle() == null ||
                    unwantedSectionsForRecording.contains(section.getTitle()))
            {
                unwantedSections.add(section);
            }
        }
        sections.removeAll(unwantedSections);
    }

    private void initRecordingData() {
        for(int i = 0; i < numberOfScreens; ++i)
        {
            String filePath = fileManager.getFilePath(wikipage.getTitle(),
                    i)
                    + "." + recorder.format;
            File recordingFile = new File(filePath);
            if(this.recordingDataList == null)
            {
                this.recordingDataList = new ArrayList<>();
            }
            this.recordingDataList.add(new SectionRecordingData(wikipage.getSection(i).getTitle(),
                    recordingFile, 0));
        }
    }

    private void initVars() {
        sectionView = findViewById(R.id.section_view);
        progressBar = findViewById(R.id.progressBar);
        loadingContent = findViewById(R.id.loadingContent);
        fileManager = new FileManager(this);
        activity = this;
        recorder = new ARecorder();
        format  = recorder.format;
        progressCounter = findViewById(R.id.progressBarCounter);
        sectionTitle = findViewById(R.id.sectionTitleView);
        showSectionsButton = findViewById(R.id.showSectionsButton);
        appData = ((WikiAudioApp) activity.getApplication()).getAppData();
        screenCounter = 0;
        recordingDataList = new ArrayList<>();
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
        milSeconds = 0;

        // init Buttons:
//        pauseButton = findViewById(R.id.pauseRecording);
        stopButton = findViewById(R.id.stopRecording);
        uploadButton = findViewById(R.id.uploadButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        playButton = findViewById(R.id.playSection);
        recordButton = findViewById(R.id.recoredButton);
        deleteRecording = findViewById(R.id.deleteRecording);

        // init timer:
        recordingTimerView = findViewById(R.id.timerView);
        recordingTimer = new CountDownTimer( Long.MAX_VALUE , 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                milSeconds++;
                updateTimerText(milSeconds);
            }

            @Override
            public void onFinish() {            }
        };
    }

    private void updateTimerText(long milSeconds) {
        long millis = milSeconds;
        int seconds = (int) (millis / 60);
        int minutes = seconds / 60;
        seconds     = seconds % 60;
        recordingTimerView.setText(String.format("%d:%02d:%02d", minutes, seconds,millis));
    }

    private void setSwipeDetector() {
        SwipeFunctions sf = new SwipeFunctions() {
            @Override
            public void onRightSwipe() {
                if(curSection > 0)
                {
                    Log.d("Swipe Detector", "right");
                    --curSection;
                    updateUI(curSection);
                    progressBar.setProgress(curSection);
                }
            }

            @Override
            public void onLeftSwipe() {
                if(curSection + 1 < wikipage.numberOfSections())
                {
                    Log.d("Swipe Detector", "left");
                    ++curSection;
                    updateUI(curSection);
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

    private void updateProgressCounter(int curSection) {
        String counterText = String.valueOf(curSection + "/" + numberOfScreens);
        progressCounter.setText(counterText);
    }

    private void updateUI(int curSection) {
        if(startRecording) // next press we want to start recording
        {
            recordButton.setImageDrawable(ContextCompat.getDrawable(activity,
                    R.drawable.recorder_icon));
            stopBlinkingAnimation(recordButton);
            recordingTimer.cancel();
        }
        else // we are currently recording
        {

        }

        if(sectionNumberIsLegal(curSection))
        {
            progressBar.setProgress(curSection + 1); // we start at one
            updateProgressCounter(curSection + 1); // we start at one
            loadWikiDataToView(curSection);
            if(existsRecording(curSection))
            {
                this.deleteRecording.setVisibility(View.VISIBLE);
                this.playButton.setVisibility(View.VISIBLE);
                this.recordingTimerView.setVisibility(View.GONE);
//            this.pauseButton.setVisibility(View.GONE);
                this.stopButton.setVisibility(View.GONE);
                this.recordButton.setVisibility(View.GONE);

            }
            else
            {
                resetTimer();
                this.recordingTimerView.setVisibility(View.VISIBLE);
                this.deleteRecording.setVisibility(View.GONE);
                this.playButton.setVisibility(View.GONE);
//            this.pauseButton.setVisibility(View.VISIBLE);
                this.stopButton.setVisibility(View.VISIBLE);
                this.recordButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void resetTimer()
    {
        milSeconds = 0; // reset timer.
        updateTimerText(milSeconds);
    }

    private boolean existsRecording(int curSection) {
        if(sectionNumberIsLegal(curSection))
        {
            return recordingDataList != null
                    && curSection < recordingDataList.size()
                    && recordingDataList.get(curSection).fileRecording != null
                    && recordingDataList.get(curSection).fileRecording.exists()
                    && recordingDataList.get(curSection).fileRecording.length() > 0;
        }
        return false;
    }

    private void loadWikiDataToView(int curSection) {
        if (sectionNumberIsLegal(curSection)) {
            sectionTitle.setText(wikipage.getSection(curSection).getTitle());
            String htmlText = "<b>" +  wikipage.getSection(curSection).getTitle()
                    + "</b>" + ".\n" + wikipage.getSection(curSection).getContents();
            sectionView.loadDataWithBaseURL("https://en.wikipedia.org",
                    htmlText,
                    "text/html",
                    "UTF-8",
                    null);
        }
    }

    private boolean sectionNumberIsLegal(int curSection) {
        return wikipage != null
                && wikipage.getSections() != null
                && curSection < wikipage.getSections().size();
    }

    private void setOnClickButtons() {
        setOnClickRecord();
        setOnClickNext();
        setOnClickPrevious();
        setOnClickUpload();
        setOnClickPlay();
        setOnClickStop();
        setOnClickDelete();
        setOnShowSectionsClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(curSection);
    }

    private void setOnClickStop() {
        this.stopButton.setOnClickListener(v -> {
            stopRecording();

            // update recording UI. todo move into updateUI() method
            updateUI(curSection);
        });
    }

//    private void setOnClickPause() {
//    }

    private void setOnClickDelete() {
        deleteRecording.setOnClickListener(v -> {
            if(this.recordingDataList != null
                    && this.recordingDataList.size() > curSection
                    && this.recordingDataList.get(curSection) != null)
            {
                SectionRecordingData sectionRecordingData = this.recordingDataList.get(curSection);
                if(sectionRecordingData.fileRecording != null)
                {
                    try {
                        PrintWriter writer = null;
                            writer = new
                                    PrintWriter(sectionRecordingData.fileRecording.getPath());
                        writer.print("");
                        // other operations
                        writer.close();
                        updateUI(curSection);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "there was a problem with deleting the " +
                                "recording file", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void setOnShowSectionsClick() {
        showSectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                SectionsDialog introDialog = new SectionsDialog(recordingDataList);
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
                SimpleAlertDialogFragment beSureTwo =
                        new SimpleAlertDialogFragment(getResources()
                                .getString(R.string.license_data),
                                "yes. upload recording.",
                                "go back",
                                (DialogInterface.OnClickListener) (dialog, which) -> {
//                                    appData.addToMyUploadedRecordings(wikipage.getTitle()); todo finish
                                    for (int i = 0; i < numberOfScreens; i++) {
                                        if (existsRecording(i)) {
                                            String path =
                                                    recordingDataList.get(i)
                                                            .fileRecording.getPath();
                                            Holder
                                                    .wikipedia
                                                    .uploadFile(wikipage.getTitle()
                                                                    + "_" + i,
                                                    path);
                                        }
                                    }
                                    Toast.makeText(activity, "thank you! we are uploading your," +
                                            " recording and it will be available on the app." +
                                            " this might take some time. ", Toast.LENGTH_LONG).show();
                                },
                                (dialog, which) -> {
                                   // what to do if he says no?

                                },
                                getString(R.string.upload_dialog_title),
                                getDrawable(R.drawable.upload));

                SimpleAlertDialogFragment beSureOne = new SimpleAlertDialogFragment(
                        "this action will upload your recording to wikipedia." +
                                "are you sure you finished you recording? ",
                        "yes. upload recording.",
                        "no i want to continue working",
                        (dialog, which) -> beSureTwo.show(fragmentManager, "beSureTwo"),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(activity, "no hurry", Toast.LENGTH_SHORT).show();
                            }
                        }
                        ,
                        getString(R.string.upload_dialog_title),
                        getDrawable(R.drawable.upload));
                beSureOne.show(fragmentManager, "beSureOne");
            }
        });
    }

    private void setOnClickPrevious() {
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Swipe Detector", "right");
                stopRecording();
                if(curSection > 0)
                {
                    curSection--;
                    updateUI(curSection);
                }
            }
        });
    }

    private void setOnClickNext() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Swipe Detector", "left");
                stopRecording();
                if(wikipage.getSections() != null &&
                        curSection + 1 < wikipage.getSections().size())
                {
                    ++curSection;
                    updateUI(curSection);
                }
            }
        });
    }

    private void setOnClickRecord() {
        recordButton.setOnClickListener(v -> {
            boolean havePermissions = checkRecordingPermissions();
//                VoiceRecorder voiceRecorder  = new VoiceRecorder(fp, activity);
            if (startRecording && havePermissions) {
                // we want to start recording and we have permissions
                if(recordingPaused)
                {
                    recorder.resumeRecording();
                }
                else
                {
                    if(recordingDataList.get(curSection).fileRecording != null)
                    {
                        recorder.startRecording(recordingDataList.get(curSection).fileRecording);
                        recodedInitialized = true;
                    }
                }
                recordingPaused = false;
                startRecording = false;
                recordButton.setImageDrawable(ContextCompat.getDrawable(activity,
                        R.drawable.pause_icon));
                startBlinkingAnimation(recordButton);
                recordingTimer.start();
            } else if (!havePermissions) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQ_RECORD_PERMISSION);
            } else {
                // we want to pause recording.
                recorder.pauseRecording();
                recordingPaused = true;
                startRecording = true;
                recordingTimer.cancel();  // stop increasing timer
                recordButton.setImageDrawable(ContextCompat.getDrawable(activity,
                        R.drawable.recorder_icon));
                stopBlinkingAnimation(recordButton);
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

    private void stopRecording()
    {
        if(recodedInitialized)
        {
            recordingDataList.get(curSection).milSeconds = milSeconds;
            recordingPaused = false;
            startRecording = true;
            recorder.stopRecording();
            recodedInitialized = false;
            updateUI(curSection);
        }
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
                getString(R.string.dialog_record_intro),
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
                },
        "Welcome to the recording area!",
                getDrawable(R.drawable.recorder_icon));
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


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CUR_SECTION_TAG, curSection);
        }
}