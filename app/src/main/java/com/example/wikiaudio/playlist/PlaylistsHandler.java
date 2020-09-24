package com.example.wikiaudio.playlist;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.Holder;
import com.example.wikiaudio.activates.playlist_ui.PlaylistFragment;
import com.example.wikiaudio.wikipedia.Wikipage;

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
    private static List<PlaylistFragment> playlistFragments = new ArrayList<>();
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

    public static void addPlaylistFragment(PlaylistFragment playlistFragment) {
        if (playlistFragment != null) {
            playlistFragments.add(playlistFragment);
        } else {
            Log.d(TAG, "add: got null playlistFragment");
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

    public void createCategoryBasedPlaylists(List<String> categories) {
        if (categories != null && categories.size() > 0) {
            for (String category : categories)
                PlaylistsHandler.addPlaylist(new Playlist(category, false, 0, 0));
        }
    }

    public static void displayNearbyPlaylistOnTheMap() {
        if (nearby == null) {
            // todo create it?
            return;
        }
        Holder.locationHandler.markPlaylist(nearby);
    }

    public Playlist getPlaylistByTitle(String playlistTitle) {
        for (Playlist playlist: playlists) {
            if (playlist.getTitle().equals(playlistTitle))
                return playlist;
        }
        return null;
    }

    public Wikipage getWikipageByPlaylistTitleAndIndex(String playlistTitle, int index) {
        Playlist playlist = getPlaylistByTitle(playlistTitle);
        return playlist.getWikipageByIndex(index);
    }

    public Playlist createSearchBasedPlaylist(String query) {
        return new Playlist(query, "search");
    }

}
