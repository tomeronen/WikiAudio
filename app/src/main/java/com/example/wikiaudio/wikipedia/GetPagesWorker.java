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

/**
 * a class of workers that get wiki pages nearby.
 */
public class GetPagesWorker extends Worker {

    public static final String latitudeTag = "Latitude";
    public static final String longitudeTag = "Longitude";
    public static final String IdTag = "pagesID";

    private String latitude;
    private String longitude;


    /**
     * constructs a worker.
     * @param context The application of the worker.
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public GetPagesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        latitude = getInputData().getString(latitudeTag);
        longitude = getInputData().getString(longitudeTag);
    }

    /**
     * does the actual background processing.  This method is called on a
     * background thread - you are required to synchronously do your work and return the
     * ListenableWorker.Result from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed.
     * @return ListenableWorker.Result
     */
    @NonNull
    @Override
    public Result doWork() {
        Call<Object> callToGetPagesNearby = WikiServerHolder
                                                    .getInstance()
                                                    .getPagesNearby(latitude, longitude);
        Response<Object> response;
        try {
            response = callToGetPagesNearby.execute();
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
