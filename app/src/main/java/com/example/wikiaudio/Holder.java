package com.example.wikiaudio;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.example.wikiaudio.location.LocationHandler;
import com.example.wikiaudio.activates.playlist.PlaylistsManager;
import com.example.wikiaudio.wikipedia.Wikipedia;

/**
 * Here is where all of the app's facades and singletons are stored for easy init and access.
 */
public class Holder {
    private static Holder instance = null;

    public static Wikipedia wikipedia;
    public static PlaylistsManager playlistsManager;
    public static LocationHandler locationHandler;
    public static MediaPlayer mediaPlayer;

    private Holder(AppCompatActivity activity){
        wikipedia = new Wikipedia(activity);
        locationHandler = LocationHandler.getInstance(activity);
        playlistsManager = PlaylistsManager.getInstance(activity);
    }

    public static Holder getInstance(AppCompatActivity activity) {
        if (instance == null) {
            instance = new Holder(activity);
        }
        return instance;
    }

    public static void setMediaPlayer(MediaPlayer newMediaPlayer) {
        mediaPlayer = newMediaPlayer;
    }

}
