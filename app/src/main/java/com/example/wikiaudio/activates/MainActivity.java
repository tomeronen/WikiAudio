package com.example.wikiaudio.activates;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.location.LocationTracker;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.google.android.gms.location.LocationCallback;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Wikipedia wikipedia;
    LocationTracker locationTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wikipedia = Wikipedia.getInstance();
//        wikipedia.getPagesNearby(32.443814,34.892546);
        showCategories();

        locationTracker = new LocationTracker(this);

    }

    private void showCategories() {
        UUID loadCategoriesId = wikipedia.loadSpokenPagesCategories(this);

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(loadCategoriesId)
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo == null) {

                        }
                        if (workInfo.getState() == WorkInfo.State.FAILED)
                        {

                        }
                        else if (workInfo.getState() == WorkInfo.State.SUCCEEDED)
                        {

                        }
                        else
                        {

                        }
                    }
                });
    }
}
