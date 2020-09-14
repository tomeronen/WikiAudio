package com.example.wikiaudio;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.material.internal.ContextUtils;
import com.google.gson.Gson;

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
    }

    public void saveChosenCategories(List<String> newChosenCategories)
    {
        chosenCategories = newChosenCategories;
        String chosenCategoriesString = gson.toJson(newChosenCategories);
        sp.edit().putString(chosenCategoriesSpTag, chosenCategoriesString).apply();

    }


    public List<String> getChosenCategories() {
        return chosenCategories;
    }
}
