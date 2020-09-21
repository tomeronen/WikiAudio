package com.example.wikiaudio;

import android.app.Application;

import com.example.wikiaudio.wikipedia.Wikipage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WikiAudioApp extends Application {

    private AppData appData;
    private ExecutorService executorService; // the thread pool of the app.
    private static final int TOTAL_THREADS = 14; //todo test what is best amount of threads.
    private List<Wikipage> playList;

    @Override
    public void onCreate() {
        super.onCreate();
        appData = new AppData(this);
        appData.loadData();
        executorService = Executors.newFixedThreadPool(TOTAL_THREADS);
        playList = new ArrayList<>();

    }

    public AppData getAppData()
    {
        return appData;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
