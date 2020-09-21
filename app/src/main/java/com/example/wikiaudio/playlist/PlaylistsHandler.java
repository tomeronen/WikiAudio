package com.example.wikiaudio.playlist;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.WikiAudioApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Here is where all the playlists are saved and monitored.
 */
public class PlaylistsHandler {
    private static final String TAG = "PlaylistsHandler";

    private static PlaylistsHandler instance = null;

    private AppCompatActivity activity;

    private static List<Playlist> playlists = new ArrayList<>();
    private static Playlist nearby;


    private PlaylistsHandler(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static PlaylistsHandler getInstance(AppCompatActivity activity) {
        if (instance == null) {
            instance = new PlaylistsHandler(activity);
        }
        return instance;
    }

    public static void addPlaylist(Playlist playlist) {
        if (playlist != null) {
            playlists.add(playlist);
        } else {
            Log.d(TAG, "add: got null playlist");
        }
    }

    public static List<Playlist> getPlaylists() {
        return playlists;
    }

    public void createLocationBasedPlaylist(double lat, double lon, boolean isNearby) {
        Playlist playlist = new Playlist(true, lat, lon);
        if (isNearby) {
            if (nearby != null)
                playlists.remove(0);
            playlists.add(0, playlist);
            nearby = playlist;
        } else {
            addPlaylist(playlist);
        }
    }

    public void createCategoryBasedPlaylists(AppCompatActivity activity) {
        //Fetch user's chosen categories
        List<String> chosenCategories = ((WikiAudioApp) activity.getApplication())
                .getAppData().getChosenCategories();
        //Create a playlist foreach category and add it to our playlists list
        if (chosenCategories != null && chosenCategories.size() > 0) {
            for (String category : chosenCategories)
                PlaylistsHandler.addPlaylist(new Playlist(category, false, 0, 0));
        }
    }

}
