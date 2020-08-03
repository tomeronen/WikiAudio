package com.example.wikiaudio.activates;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.wikiaudio.R;
import com.example.wikiaudio.location.LocationTracker;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.google.android.gms.location.LocationCallback;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Wikipedia wikipedia;
    LocationTracker locationTracker;
    AppCompatActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wikipedia = Wikipedia.getInstance();

        wikipedia.login("a","b");
        // for debug:
//        Intent rec = new Intent(this, WikiRecordActivity.class);
//        startActivity(rec);
//        activity = this;
//        wikipedia = Wikipedia.getInstance();
//        wikipedia.getPagesNearby(this,32.443814,34.892546);
//        showCategories();
//        locationTracker = new LocationTracker(this);

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
                            CategoryAdapter categoryAdapter =
                                    new CategoryAdapter(activity,
                                            Wikipedia.getInstance().spokenPagesCategories,
                                    new CategoryClickListeners(){

                                        @Override
                                        public void onClick(String string) {
                                            Toast.makeText(activity, string, Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            RecyclerView recyclerView = findViewById(R.id.categorys);
                            recyclerView.setLayoutManager(new GridLayoutManager(activity,
                                    3));
                            recyclerView.setAdapter(categoryAdapter);
                        }
                        else
                        {

                        }
                    }
                });
    }
}
