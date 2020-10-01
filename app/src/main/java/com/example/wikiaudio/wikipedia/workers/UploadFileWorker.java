package com.example.wikiaudio.wikipedia.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.wikipedia.server.WikiServerHolder;
import com.example.wikiaudio.wikipedia.server.WikiUserData;

import java.io.IOException;
import java.util.List;


public class UploadFileWorker extends Worker {
    public static final String FILE_PATH_TAG = "filePath";
    public static final String FILE_NAME_TAG = "fileName";
    private final String filePath;
    private final String fileName;
    private Context context;

    public UploadFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        filePath = getInputData().getString(FILE_PATH_TAG);
        fileName = getInputData().getString(FILE_NAME_TAG);

    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("upload file status", "starting to load file:" + fileName);
        if(fileName != null && filePath != null) {
            try {
                List<WikiUserData> usersData = Holder.wikipedia.getUsersData();
                for(WikiUserData userData: usersData) { //  if the upload was successful stop.
                    WikiServerHolder.getInstance().uploadFile(fileName, filePath, userData);
                }
                return Result.success();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("upload file status:", "there was a problem with loading"
                        + fileName + " trying again");
                return Result.retry();
            }
        }
        return Result.failure();
    }

}

