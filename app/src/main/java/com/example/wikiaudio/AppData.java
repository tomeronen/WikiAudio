package com.example.wikiaudio;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AppData
{
    private static final String chosenCategoriesSpTag = "chosenCategories";
    private final Gson gson;

    private WikiAudioApp wikiAudioApp;
    SharedPreferences sp;
    static List<String> chosenCategories;



    public AppData(WikiAudioApp wikiAudioApp) {
        this.wikiAudioApp = wikiAudioApp;
        gson  = new Gson();
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

    public void saveChosenCategories(List<String> newChosenCategories)
    {
        chosenCategories = newChosenCategories;
        String chosenCategoriesString = gson.toJson(newChosenCategories);
        sp.edit().putString(chosenCategoriesSpTag, chosenCategoriesString).apply();

    }


    public List<String> getChosenCategories() {
        if(chosenCategories == null)
        {
            chosenCategories = new ArrayList<>();
        }
        return chosenCategories;
    }
}
