package com.example.wikiaudio;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.location.LocationHandler;
import com.example.wikiaudio.playlist.PlaylistsHandler;
import com.example.wikiaudio.wikipedia.Wikipedia;

public class Handler {
    private static Handler instance = null;

    public static Wikipedia wikipedia;
    public static PlaylistsHandler playlistsHandler;
    public static LocationHandler locationHandler;

    private Handler(AppCompatActivity activity){
        wikipedia = new Wikipedia(activity);
        locationHandler = LocationHandler.getInstance(activity);
        playlistsHandler = PlaylistsHandler.getInstance(activity);
    }

    public static Handler getInstance(AppCompatActivity activity) {
        if (instance == null) {
            instance = new Handler(activity);
        }
        return instance;
    }

}
