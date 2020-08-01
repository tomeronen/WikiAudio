package com.example.wikiaudio.wikipedia;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class loadSpokenCategoriseWorker extends Worker {

    public loadSpokenCategoriseWorker(@NonNull Context context,
                                      @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // TODO - not finished just for basic debugging
        Call<Object> call = WikiServerHolder.getInstance().callGetSpokenPagesCategories();
        Response<Object> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            // TODO -- what to do if fails.
            e.printStackTrace();
        }
        if (response.code() != 200 || !response.isSuccessful()) {
            // TODO -- what to do if fails.
            return Result.failure();
        } else {
            ArrayList<String> allCategories = new ArrayList<>();
            String a = response.body().toString();
            LinkedTreeMap<String, LinkedTreeMap<String, Object>> b = (LinkedTreeMap<String, LinkedTreeMap<String, Object>>) response.body();
            ArrayList<LinkedTreeMap<String, Object>> c = (ArrayList<LinkedTreeMap<String, Object>>) b.get("parse").get("sections");
            for (int i = 0; i < c.size(); ++i) {
                allCategories.add((String) c.get(i).get("line"));
            }
            Wikipedia.getInstance().spokenPagesCategories = allCategories;
            return Result.success();
        }
    }
}
