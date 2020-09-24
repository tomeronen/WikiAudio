package com.example.wikiaudio.file_manager;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class FileManager {

    Activity activity;

    public FileManager(Activity activity) {
        this.activity = activity;
    }

    public String getFilePath(String wikiName, int section, int paragraph)
    {
        File file = new File(activity.getExternalFilesDir(
                Environment.DIRECTORY_MUSIC),
                wikiName);
        Log.d("file Path:",  file.getAbsolutePath());
        Log.d("im here:", "true");
        file.mkdirs();
        if (!file.exists())
        {
            if(!file.mkdirs())
            {
                Log.e("file problem:", "Directory not created");
            }
        }
        return file.getAbsolutePath() + "/" + wikiName + "_" + section + "_" + paragraph;

//        File f = new File(filePath);
//        if(f.exists())
//        {
//            return filePath;
//        }
//        else
//        {
//            f.mkdir();
//            return filePath;
//        }
    }
}

