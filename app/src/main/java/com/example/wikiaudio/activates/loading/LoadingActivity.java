package com.example.wikiaudio.activates.loading;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.Wikipage;

public class LoadingActivity extends Activity {
    //Introduce a delay
    private final int WAIT_TIME = 30;
    private static final String TAG = "LoadingActivity";

    private TextView txtProgress;
    private ProgressBar progressBar;
    private Wikipage wikipage;

    private int pStatus = 0;
    private int waitTime = WAIT_TIME;

    private Handler handler = new Handler();
    private LoadingHelper loadingHelper = LoadingHelper.getInstance();

    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        txtProgress = (TextView) findViewById(R.id.txtProgress);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();
        index = intent.getIntExtra("index", -1);
        fetchWikipage();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (pStatus <= 100) {
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

    private void fetchWikipage(){
        if (index == -1) {
            Log.d(TAG, "onCreate: got error wikipage index");
            finish();
        }
        wikipage = loadingHelper.getWikipageByIndex(index);
        if (wikipage == null){
            Log.d(TAG, "onCreate: got error wikipage element");
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (index == -1 || !loadingHelper.removeWikipage(index)){
            Log.d(TAG, "onDestroy: got an error for deleting wikipage from LoadingHelper");
        }
    }
}