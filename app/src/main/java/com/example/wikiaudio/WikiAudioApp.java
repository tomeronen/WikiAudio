package com.example.wikiaudio;

import android.app.Application;
import android.content.IntentFilter;

public class WikiAudioApp extends Application {

    private AppData appData;

    @Override
    public void onCreate() {
        super.onCreate();
        appData = new AppData(this);
        appData.loadData();
    }

    public AppData getAppData()
    {
        return appData;
    }

}