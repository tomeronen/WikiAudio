package com.example.wikiaudio;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.wikiaudio.data.AppData;

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
    private static final int TOTAL_THREADS = 20; //todo test what is best amount of threads.
    private ExecutorService executorService; // the thread pool of the app.

    private AppData appData;
    private Activity activeActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        appData = new AppData(this);
        setupActivityListener();
        executorService = Executors.newFixedThreadPool(TOTAL_THREADS);
    }

    public AppData getAppData() {
        return appData;
    }

    private void setupActivityListener() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
            }
            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }
            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                activeActivity = activity;
            }
            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                activeActivity = null;
            }
            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }
            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }
            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }

    public Activity getActiveActivity() {
        return activeActivity;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}
