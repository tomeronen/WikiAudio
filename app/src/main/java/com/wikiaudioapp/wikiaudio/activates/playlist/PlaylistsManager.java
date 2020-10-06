package com.wikiaudioapp.wikiaudio.activates.playlist;

import android.location.Location;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.wikiaudioapp.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.wikiaudioapp.wikiaudio.data.Holder;
import com.wikiaudioapp.wikiaudio.wikipedia.server.WorkerListener;
import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.Wikipage;

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
        PlaylistsManager.activity = activity;
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
    public synchronized void  createLocationBasedPlaylist(double lat, double lon, boolean isNearby) {
        Playlist playlist = null;
        if(nearby != null // check if we already have a good locationPlayList
                && nearby.getLat() != 0
                && nearby.getLon() != 0)
        {
            float[] results = new float[1]; // we only want distance (length one)
            Location.distanceBetween(nearby.getLat(), nearby.getLon(),
                    lat, lon, results);
            if(results[0] < 50) // we have a up to date Location Based Playlist
            {
                playlist = nearby;
            }
        }

        if(playlist == null) // we don't have a nearby playlist already. make one.
        {
            playlist = new Playlist(true, lat, lon);
        }
        if (isNearby) {
            //replace the nearby playlist if it exists
            if (playlists.size() > 0 && playlists.get(0).getTitle().equals("Nearby")) {
                playlists.set(0, playlist);
            } else {
                //ow, just add it to the beginning
                playlists.add(0, playlist);
            }
            nearby = playlist;
        } else {
            addPlaylist(playlist);
        }

    }

    /**
     * Creates a playlist for each given category
     */
    public void createCategoryBasedPlaylists(List<String> categories) {
        if (!categoryBasedPlaylistsWereCreated && categories != null && categories.size() > 0) {
            categoryBasedPlaylistsWereCreated = true;
            for (String category: categories)
                if (getPlaylistByTitle(category) == null) {
                    //The category was yet to be created
                    addPlaylist(new Playlist(category, false, 0, 0));
                }
        }
    }

    /**
     * Given a new list of categories, updates the current playlist list by adding the ones on
     * both the old and the new list & creating the new ones - when needed.
     */
    public void updateCategoryBasedPlaylists(List<String> categories) {
        if (categories == null) {
            Log.d(TAG, "updateCategoryBasedPlaylists: null categories list");
            return;
        }
        List<Playlist> newPlaylists = new ArrayList<>();
        if (nearby != null) {
            newPlaylists.add(0, getNearby());
        }
        for (String category: categories) {
            Playlist playlist = getPlaylistByTitle(category);
            if (playlist == null) {
                newPlaylists.add(new Playlist(category, false, 0, 0));
            } else {
                newPlaylists.add(playlist);
            }
        }
        playlists = newPlaylists;
    }

    /**
     * creates a playlist that is the wikipage search result of query value.
     * @param query the value to search.
     * @return the playlist created.
     */
    public Playlist createSearchBasedPlaylist(String query, WorkerListener workerListener) {
        searchPlaylists = new Playlist(query, "search", workerListener);
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