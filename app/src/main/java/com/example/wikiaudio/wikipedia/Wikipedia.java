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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * the facade to all interaction with wikipedia
 */
public class Wikipedia {
    private ComponentActivity ownerActivity;

    public Wikipedia(ComponentActivity ownerActivity) {
        this.ownerActivity = ownerActivity;
    }

    public List<WikiPage> getPagesNearby(double latitude , double longitude)
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

    public void getSpokenPagesCategories()
    {
        // TODO - not finished just for basic debugging
        Call<Object> call = WikiServerHolder.getInstance().callGetSpokenPagesCategories();
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                String res = response.body().toString();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {

            }
        });
    }
}
