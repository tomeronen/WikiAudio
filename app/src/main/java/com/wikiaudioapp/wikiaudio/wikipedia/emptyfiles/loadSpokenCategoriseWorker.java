//package com.example.wikiaudio.wikipedia;
//
//import android.content.Context;
//
//import androidx.annotation.NonNull;
//import androidx.work.Data;
//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import com.google.gson.Gson;
//import com.google.gson.internal.LinkedTreeMap;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class loadSpokenCategoriseWorker extends Worker {
//
//    public static String listTag = "listTag";
//    private List outputList;
//
//    public loadSpokenCategoriseWorker(@NonNull Context context,
//                                      @NonNull WorkerParameters workerParams) {
//        super(context, workerParams);
//    }
//
//    @NonNull
//    @Override
//    public Result doWork() {
//        // TODO - not finished just for basic debugging
//        try {
//            List<String> categorise = WikiServerHolder.getInstance().callGetSpokenPagesCategories();
//            return Result.success();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return Result.failure();
//        }
//    }
//}
