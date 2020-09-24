package com.example.wikiaudio.activates.loading;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

public class LoadingActivity extends Activity {
    private static final String TAG = "LoadingActivity";
    private final int WAIT_TIME = 30;

    private TextView txtProgress;
    private ProgressBar progressBar;

    private int pStatus = 0;
    private int waitTime = WAIT_TIME;
    private int index;

    private Wikipage wikipage;
    private Handler handler = new Handler();
    private LoadingHelper loadingHelper = LoadingHelper.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        txtProgress = findViewById(R.id.txtProgress);
        progressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        index = intent.getIntExtra("index", -1);
        fetchWikipage();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (pStatus < 100) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(pStatus);
                            String status = pStatus + " %";
                            txtProgress.setText(status);
                        }
                    });
                    try {
                        if (wikipage.getTitle() != null) {
                            finish();
                        }
                        Thread.sleep(waitTime);
                        if (pStatus == 100) {
                            if (wikipage.getTitle() == null) {
                                Log.d(TAG, "onCreate-progressBar: wiki server is taking too long");
                            }
                            finish();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pStatus++;
                }
            }
        }).start();

    }

    /**
     * Pretty self-explanatory, really.
     */
    private void fetchWikipage(){
        if (index == -1) {
            Log.d(TAG, "onCreate: got error wikipage index");
            finish();
        }
        wikipage = loadingHelper.getWikipageByIndex(index);
        if (wikipage == null){
            Log.d(TAG, "onCreate: got an error while getting wikipage element");
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Make sure to delete the "waiting" wikipage
        if (!loadingHelper.removeWikipageByElement(wikipage)){
            Log.d(TAG, "onDestroy: got an error for deleting wikipage from LoadingHelper");
        }
    }
}