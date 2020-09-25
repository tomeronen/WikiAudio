package com.example.wikiaudio.data;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.activates.playlist.Playlist;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Cross application data
 * This class is init when the app loads
 */
public class AppData {
    private static final String CHOSEN_CATEGORIES_SP_TAG = "chosenCategories";
    private static final String LAST_LOADED_CATEGORIES_SP_TAG = "lastLoadedCategories";
    private static final String CATEGORIES_SP_TAG = "categories";
    private static final String PLAYLIST_TITLE_SP_TAG = "playlist";
    private static final String WIKIPAGE_TITLE_SP_TAG = "wikipage";
    private static final String TRACK_INDEX_SP_TAG = "trackIndex";

    private WikiAudioApp wikiAudioApp;
    private SharedPreferences sp;
    private final Gson gson = new Gson();

    //category related
    private static SortedSet<String> chosenCategories;

    //playlist related
    public CurrentlyPlayed currentlyPlayed;
    private Playlist lastPlayedPlaylist;

    public AppData(WikiAudioApp wikiAudioApp) {
        this.wikiAudioApp = wikiAudioApp;
        loadData();
    }

    private void loadData() {
        sp = PreferenceManager.getDefaultSharedPreferences(wikiAudioApp);

        //Categories
        String chosenCategoriesString = sp.getString(CHOSEN_CATEGORIES_SP_TAG, "");
        chosenCategories = gson.fromJson(chosenCategoriesString, SortedSet.class);
        if(chosenCategories == null) {
            chosenCategories = new TreeSet<>();
        }

        //Playlist
        currentlyPlayed = new CurrentlyPlayed();
        String playlistTitle = sp.getString(PLAYLIST_TITLE_SP_TAG, "");
        String wikipageTitle = sp.getString(WIKIPAGE_TITLE_SP_TAG, "");
        int trackIndex = sp.getInt(TRACK_INDEX_SP_TAG, -1);
        //todo continue
    }

    public WikiAudioApp getWikiAudioApp() {
        return wikiAudioApp;
    }

    //TODO why are there 3 different set/get categories func? please add some documentation
    //Category related: we want to save user's favorite categories
    public void saveChosenCategories(List<String> newChosenCategories)
    {
        if(newChosenCategories != null)
        {
            chosenCategories = new TreeSet<>(newChosenCategories);
            String chosenCategoriesString = gson.toJson(newChosenCategories);
            sp.edit().putString(CHOSEN_CATEGORIES_SP_TAG, chosenCategoriesString).apply();
        }
    }

    public List<String> getChosenCategories() {
        if(chosenCategories == null)
        {
            chosenCategories = new TreeSet<>();
        }
        return new ArrayList<>(chosenCategories);
    }

    public Date getLastLoadedCategories() {
        String lastLoadedCategories = sp.getString(LAST_LOADED_CATEGORIES_SP_TAG, "");
        if(!lastLoadedCategories.equals(""))
        {
            return gson.fromJson(lastLoadedCategories, Date.class);
        }
        return null;
    }

    public void setLastLoadedCategories(Date newDate) {
        if(newDate != null)
        {
            String dateString = gson.toJson(newDate);
            sp.edit().putString(LAST_LOADED_CATEGORIES_SP_TAG, dateString).apply();
        }
    }

    public List<String> getCategories() {
        String categoriesString = sp.getString(CATEGORIES_SP_TAG, "");
        if(!categoriesString.equals(""))
        {
            return gson.fromJson(categoriesString, List.class);
        }
        return null;
    }

    public void setCategories(List<String> categories) {
        if(categories != null)
        {
            String categoriesString = gson.toJson(categories);
            sp.edit().putString(CATEGORIES_SP_TAG, categoriesString).apply();
        }
    }


    //Playlist related: we want to save user's last played playlist
    public void setLastPlayedPlaylist(Playlist currentlyPlaying) {
        this.lastPlayedPlaylist = currentlyPlaying;
    }

    public Playlist lastPlayedPlaylist() {
        return lastPlayedPlaylist;
    }

    //
    public WikiAudioApp getWikiAudioApp() {
        return wikiAudioApp;
    }

    public void setCurrentlyPlayed(CurrentlyPlayed currentlyPlayed) {
        this.currentlyPlayed = currentlyPlayed;
    }
    public CurrentlyPlayed getCurrentlyPlayed() {
        return currentlyPlayed;
    }
}
