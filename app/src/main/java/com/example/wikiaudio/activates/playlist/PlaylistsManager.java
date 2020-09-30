package com.example.wikiaudio.activates.playlist;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

import java.util.ArrayList;
import java.util.List;

/**
 * Here is where all the playlists are saved and monitored.
 */
public class PlaylistsManager {
    private static final String TAG = "PlaylistsHandler";

    private static PlaylistsManager instance = null;

    private static AppCompatActivity activity;
    private static List<Playlist> playlists = new ArrayList<>();
    private static Playlist nearby;
    private static Playlist searchPlaylists;

    private MediaPlayer mediaPlayer;

    private static boolean categoryBasedPlaylistsWereCreated = false;


    private PlaylistsManager(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static PlaylistsManager getInstance(AppCompatActivity activity) {
        if (instance == null) {
            instance = new PlaylistsManager(activity);
        }
        return instance;
    }

    /**
     * Creates a playlist based on location
     * @param isNearby if true then overrides the current nearby playlist
     */
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
        if (!categoryBasedPlaylistsWereCreated) {
            categoryBasedPlaylistsWereCreated = true;
            if (categories != null && categories.size() > 0) {
                for (String category : categories)
                    if(getPlaylistByTitle(category) == null) {
                        // the category was not yet created.
                        PlaylistsManager.addPlaylist(new Playlist(category, false, 0, 0));
                    }
            }
        }
    }

    /**
     * creates a playlist that is the wikipage search result of query value.
     * @param query the value to search.
     * @return the playlist created.
     */
    public Playlist createSearchBasedPlaylist(String query) {
        searchPlaylists = new Playlist(query, "search");
        return searchPlaylists;

    }

    public static void addPlaylist(Playlist playlist) {
        if (playlist != null) {
            playlists.add(playlist);
        } else {
            Log.d(TAG, "add: got null playlist");
        }
    }

    public static void displayNearbyPlaylistOnTheMap() {
        if (nearby == null) {
            Log.d(TAG, "displayNearbyPlaylistOnTheMap: null nearby playlist, nothing to display");
            return;
        }
        Holder.locationHandler.markPlaylist(nearby);
    }

    public Playlist getPlaylistByTitle(String playlistTitle) {
        for (Playlist playlist: playlists) {
            if (playlist.getTitle().equals(playlistTitle))
                return playlist;
        }
        if (searchPlaylists != null && searchPlaylists.getTitle().equals(playlistTitle)) {
            return searchPlaylists;
        }
        return null;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mPlayer) {
        mediaPlayer = mPlayer;
    }

    public static List<Playlist> getPlaylists() {
        return playlists;
    }

    public int getIndexByPlaylist(Playlist playlist) {
        return playlists.indexOf(playlist);
    }

    public Wikipage getWikipageByPlaylistTitleAndIndex(String playlistTitle, int index) {
        Playlist playlist = getPlaylistByTitle(playlistTitle);
        return playlist.getWikipageByIndex(index);
    }

    public static AppCompatActivity getActivity() {
        return activity;
    }

    public Playlist getNearby() {
        return nearby;
    }

}