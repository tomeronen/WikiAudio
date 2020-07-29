package com.example.wikiaudio.wikipedia;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class GetPagesWorker extends Worker {

    public static final String latitudeTag = "Latitude";
    public static final String longitudeTag = "Longitude";
    public static final String IdTag = "pagesID";

    private String latitude;
    private String longitude;


    public GetPagesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        latitude = getInputData().getString(latitudeTag);
        longitude = getInputData().getString(longitudeTag);
    }

    @NonNull
    @Override
    public Result doWork() {
        Call<Object> callToGetToken = WikiServerHolder
                                                    .getInstance()
                                                    .getPagesNearby(latitude, longitude);
        Response<Object> response = null;
        try {
            response = callToGetToken.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (response.code() != 200 || !response.isSuccessful()) {
            return Result.failure();
        }
        else
        {
            String result =new Gson().toJson(response.body());
            if (result == null)
            {
                return Result.failure();
            }
            return Result.success(new Data.Builder().putString(IdTag, result).build());
        }
    }
}
