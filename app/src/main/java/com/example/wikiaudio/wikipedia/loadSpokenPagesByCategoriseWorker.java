package com.example.wikiaudio.wikipedia;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class loadSpokenPagesByCategoriseWorker extends Worker {
    public static final String categoryTag = "categoryTag";
    String category;

    public loadSpokenPagesByCategoriseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        category = getInputData().getString(categoryTag);
    }

    @NonNull
    @Override
    public Result doWork() {
        Response<Object> response = null;
        try {
            response = WikiServerHolder.getInstance().callGetSpokenPagesByCategories(category).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
        if (response.code() != 200 || !response.isSuccessful()) {
            // TODO -- what to do if fails.
            return Result.failure();
        } else {
            ArrayList<String> links = new ArrayList<>();
            LinkedTreeMap<String, LinkedTreeMap<String, ArrayList<LinkedTreeMap<String, Object>>>> a = (LinkedTreeMap<String, LinkedTreeMap<String, ArrayList<LinkedTreeMap<String, Object>>>>) response.body();
            ArrayList<LinkedTreeMap<String, Object>> c = a.get("parse").get("links");
            for (int i = 0; i < c.size(); ++i) {
                links.add((String) c.get(i).get("*"));
            }
            Wikipedia.getInstance().spokenCategories.put(category, links);
            return Result.success();
        }
    }
}
