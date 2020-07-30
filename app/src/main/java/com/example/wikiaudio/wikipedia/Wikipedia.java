package com.example.wikiaudio.wikipedia;

import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * the facade to all interaction with wikipedia
 */
public class Wikipedia {
    private ComponentActivity ownerActivity;

    public Wikipedia(ComponentActivity ownerActivity) {
        this.ownerActivity = ownerActivity;
    }

    public List<WikiPage> getPagesNearby(long latitude , long longitude)
    {
        ArrayList<WikiPage> pagesNearby = new ArrayList<>();
        WorkRequest getNearbyPagesReq =
                new OneTimeWorkRequest
                        .Builder(GetPagesNearbyWorker.class)
                        .setInputData(new Data.Builder()
                                .putString(GetPagesNearbyWorker.latitudeTag, Long.toString(latitude))
                                .putString(GetPagesNearbyWorker.longitudeTag, Long.toString(longitude))
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

}
