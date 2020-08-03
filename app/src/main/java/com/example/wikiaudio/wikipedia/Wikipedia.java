package com.example.wikiaudio.wikipedia;

import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * the facade to all interaction with wikipedia
 */
public class Wikipedia {

    public ArrayList<String> spokenPagesCategories;
    private static Wikipedia instance = null;
    LinkedTreeMap<String, ArrayList<String>> spokenCategories;

    synchronized static public Wikipedia getInstance(){
        if (instance == null) {
            instance = new Wikipedia();
        }
        return instance;
    }

    private Wikipedia(){
    }


    public List<WikiPage> getPagesNearby(ComponentActivity ownerActivity, double latitude , double longitude)
    {
        ArrayList<WikiPage> pagesNearby = new ArrayList<>();
        WorkRequest getNearbyPagesReq =
                new OneTimeWorkRequest
                        .Builder(GetPagesNearbyWorker.class)
                        .setInputData(new Data.Builder()
                                .putString(GetPagesNearbyWorker.latitudeTag, Double.toString(latitude))
                                .putString(GetPagesNearbyWorker.longitudeTag, Double.toString(longitude))
                                .build())
                        .build();
        WorkManager.getInstance(ownerActivity).enqueue(getNearbyPagesReq);
        WorkManager.getInstance(ownerActivity)
                .getWorkInfoByIdLiveData(getNearbyPagesReq.getId())
                .observe(ownerActivity, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo == null) {

                        }
                        if (workInfo.getState() == WorkInfo.State.FAILED)
                        {

                        }
                        else if (workInfo.getState() == WorkInfo.State.SUCCEEDED)
                        {
                            Log.d("got pages", "got pages");
                        }
                        else
                        {

                        }
                    }
                });
        return pagesNearby;
    }

    public UUID loadSpokenPagesCategories(ComponentActivity ownerActivity)
    {
        WorkRequest loadSpokenCategoriseWorkerReq =
                new OneTimeWorkRequest
                        .Builder(loadSpokenCategoriseWorker.class)
                        .build();
        WorkManager.getInstance(ownerActivity).enqueue(loadSpokenCategoriseWorkerReq);
        return loadSpokenCategoriseWorkerReq.getId();
    }

    public UUID loadSpokenPagesByCategories(ComponentActivity ownerActivity, final String category)
    {
        WorkRequest loadSpokenCategoriseWorkerReq =
                new OneTimeWorkRequest
                        .Builder(loadSpokenPagesByCategoriseWorker.class)
                        .setInputData(new Data.Builder()
                        .putString(loadSpokenPagesByCategoriseWorker.categoryTag, category).build())
                        .build();
        WorkManager.getInstance(ownerActivity).enqueue(loadSpokenCategoriseWorkerReq);
        return loadSpokenCategoriseWorkerReq.getId();
    }

    public void login(String a, String b) {
        WikiServerHolder.getInstance().callLogin(a,b);
    }
}
