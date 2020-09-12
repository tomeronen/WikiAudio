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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class loadSpokenPagesByCategoriseWorker extends Worker {
    public static final String categoryTag = "categoryTag";
    String category;
    private Context context;

    public loadSpokenPagesByCategoriseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        category = getInputData().getString(categoryTag);
    }

    @NonNull
    @Override
    public Result doWork() {
        List<PageAttributes> pageAttr = new ArrayList<>();
        pageAttr.add(PageAttributes.title);
        try {
            List<String> pageNames
                    = WikiServerHolder.callGetSpokenPagesNamesByCategories(category);
            // todo come back to this
//            new Wikipedia(context).spokenCategories.put(category, pageNames);
            return Result.success(new Data.Builder().putString("a","a").build());
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
