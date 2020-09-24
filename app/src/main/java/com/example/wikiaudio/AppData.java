package com.example.wikiaudio;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.wikiaudio.activates.playlist.Playlist;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Cross application data
 */
public class AppData {
    private static final String chosenCategoriesSpTag = "chosenCategories";
    private static final String lastLoadedCategoriesSpTag = "lastLoadedCategories";
    private static final String categoriesSpTag = "categories";

    private WikiAudioApp wikiAudioApp;
    private final Gson gson = new Gson();
    private SharedPreferences sp;

    //category related
    private static List<String> chosenCategories;

    //playlist related
    private Playlist lastPlayedPlaylist;


    public AppData(WikiAudioApp wikiAudioApp) {
        this.wikiAudioApp = wikiAudioApp;
    }

    public void loadData() {
        sp = PreferenceManager.getDefaultSharedPreferences(wikiAudioApp);
        String chosenCategoriesString = sp.getString(chosenCategoriesSpTag, "");
        chosenCategories = gson.fromJson(chosenCategoriesString, List.class);
        if(chosenCategories == null)
        {
            chosenCategories = new ArrayList<>();
        }
    }

    //Category related: we want to save user's favorite categories
    //TODO why are there 3 different set/get categories func? please add some documentation
    public void saveChosenCategories(List<String> newChosenCategories)
    {
        if(newChosenCategories != null)
        {
            chosenCategories = newChosenCategories;
            String chosenCategoriesString = gson.toJson(newChosenCategories);
            sp.edit().putString(chosenCategoriesSpTag, chosenCategoriesString).apply();
        }
    }

    public List<String> getChosenCategories() {
        if(chosenCategories == null)
        {
            chosenCategories = new ArrayList<>();
        }
        return chosenCategories;
    }

    public Date getLastLoadedCategories() {
        String lastLoadedCategories = sp.getString(lastLoadedCategoriesSpTag, "");
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
            sp.edit().putString(lastLoadedCategoriesSpTag, dateString).apply();
        }
    }

    public List<String> getCategories() {
        String categoriesString = sp.getString(categoriesSpTag, "");
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
            sp.edit().putString(categoriesSpTag, categoriesString).apply();
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

}