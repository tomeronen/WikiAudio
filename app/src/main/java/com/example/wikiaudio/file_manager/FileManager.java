package com.example.wikiaudio.file_manager;

import android.app.Activity;

public class FileManager {

    Activity activity;

    public FileManager(Activity activity) {
        this.activity = activity;
    }

    public String getFilePath(String wikiName, int section)
    {
        return  activity.getFilesDir() + "/" + wikiName + "_" + section + ".mp3";

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

