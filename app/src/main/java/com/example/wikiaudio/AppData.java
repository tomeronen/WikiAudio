package com.example.wikiaudio;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.wikiaudio.playlist.Playlist;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class AppData
{
    private static final String chosenCategoriesSpTag = "chosenCategories";
    private static final String lastLoadedCategoriesSpTag = "lastLoadedCategories";
    private static final String categoriesSpTag = "categories";



    private final Gson gson;

    private WikiAudioApp wikiAudioApp;
    private boolean playingStatus = false;
    private int curPositionInPlaylist = 0;
    private Playlist getPlaylist;
    SharedPreferences sp;
    static SortedSet<String> chosenCategories;


    public Playlist getPlaylist() {
        return getPlaylist;
    }



    public void setPlaylist(Playlist currentlyPlaying) {
        this.getPlaylist = currentlyPlaying;
    }


    public AppData(WikiAudioApp wikiAudioApp) {
        this.wikiAudioApp = wikiAudioApp;
        gson  = new Gson();
    }

    public void loadData() {
        sp = PreferenceManager.getDefaultSharedPreferences(wikiAudioApp);
        String chosenCategoriesString = sp.getString(chosenCategoriesSpTag, "");
        chosenCategories = gson.fromJson(chosenCategoriesString, SortedSet.class);
        if(chosenCategories == null)
        {
            chosenCategories = new TreeSet<>();
        }
    }

    public void saveChosenCategories(List<String> newChosenCategories)
    {
        if(newChosenCategories != null)
        {
            chosenCategories = new TreeSet<>(newChosenCategories);
            String chosenCategoriesString = gson.toJson(newChosenCategories);
            sp.edit().putString(chosenCategoriesSpTag, chosenCategoriesString).apply();
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

    public boolean getPlayingStatus() {
        return playingStatus;
    }
    public void setPlayingStatus(boolean playingStatus) {
        this.playingStatus = playingStatus;
    }

    public int getCurPosition() {
        return curPositionInPlaylist;
    }

    public void setCurPositionInPlaylist(int curPositionInPlaylist) {
        if(curPositionInPlaylist >= 0) {
            this.curPositionInPlaylist = curPositionInPlaylist;
        }
    }
}