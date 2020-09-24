package com.example.wikiaudio;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is primarily used for initialization of global state before the first Activity
 * is displayed. Usually used for:
 *      - Specialized tasks that need to run before the creation of the first activity
 *      - Global initialization that needs to be shared across all components
 *      - Static methods for easy access to static immutable data
 *
 * You should never store mutable shared data here since that data might disappear or become
 * invalid at any time.
 */
public class WikiAudioApp extends Application {

    private AppData appData;
    private ExecutorService executorService; // the thread pool of the app.
    private static final int TOTAL_THREADS = 14; //todo test what is best amount of threads.

    @Override
    public void onCreate() {
        super.onCreate();
        appData = new AppData(this);
        appData.loadData();
        executorService = Executors.newFixedThreadPool(TOTAL_THREADS);
    }

    public AppData getAppData()
    {
        return appData;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}
