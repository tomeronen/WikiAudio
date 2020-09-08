package com.example.wikiaudio.wikipedia;

import androidx.activity.ComponentActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Response;


/**
 * Facade singleton class to all interaction with wikipedia.
 * Main purpose is to manage requests to wikipedia and decide what is the best method
 * thread/AsyncTask/workManager.
 */
public class Wikipedia {

    public ArrayList<String> spokenPagesCategories;
    private static Wikipedia instance = null;
    LinkedTreeMap<String, ArrayList<String>> spokenCategories;

    synchronized static public Wikipedia getInstance(){
        if (instance == null) {
            instance = new Wikipedia();
        }
        return instance;
    }

    private Wikipedia(){
    }


    /**
     * gets wikiPages nearby the given coordinates. if successful,
     * fills the 'listToFill' with found pages. And then runs workerListener.
     * @param latitude the latitude to preform the search on.
     * @param longitude the longitude to preform the search on.
     * @param radius the radius from coordinates to search in.
     * @param listToFill the wikiPage list to be filled with results.
     * @param pageAttributes the attributes to get on each wiki page found.
     * @param workerListener what to do if task fails or is successful.
     */
    public void getPagesNearby(final double latitude,
                               final double longitude,
                               final int radius,
                               final List<WikiPage> listToFill,
                               final List<PageAttributes> pageAttributes,
                               final WorkerListener workerListener)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listToFill.addAll(WikiServerHolder.getPagesNearby(latitude,
                                                                        longitude,
                                                                        radius,
                                                                        pageAttributes));
                    // task was successful.
                    workerListener.onSuccess();
                } catch (IOException e) {
                    // task failed with a exception.
                    workerListener.onFailure();
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * loads the current spoken pages categories from wikipedia.
     * @param ownerActivity the activity that sends the request (for work manager).
     * @return UUID of the work (lets the caller see when and if work is successful).
     */
    public UUID loadSpokenPagesCategories(ComponentActivity ownerActivity)
    {
        WorkRequest loadSpokenCategoriseWorkerReq =
                new OneTimeWorkRequest
                        .Builder(loadSpokenCategoriseWorker.class)
                        .setInputData(new Data.Builder()
                                .build())
                        .build();
        WorkManager.getInstance(ownerActivity).enqueue(loadSpokenCategoriseWorkerReq);
        return loadSpokenCategoriseWorkerReq.getId();
    }

    public UUID loadSpokenPagesByCategories(ComponentActivity ownerActivity,
                                            final String category)
    {
        WorkRequest loadSpokenCategoriseWorkerReq =
                new OneTimeWorkRequest
                        .Builder(loadSpokenPagesByCategoriseWorker.class)
                        .setInputData(new Data.Builder()
                        .putString(loadSpokenPagesByCategoriseWorker.categoryTag, category).build())
                        .build();
        WorkManager.getInstance(ownerActivity).enqueue(loadSpokenCategoriseWorkerReq);
        return loadSpokenCategoriseWorkerReq.getId();
    }
//todo finish implement.

//    public void login(String a, String b) {
//        WikiServerHolder.getInstance().callLogin(a,b);
//    }

    public WikiPage getWikiPage(String name, List<PageAttributes> pageAttributes)
    {
        try {
            WikiPage wikiPage = WikiServerHolder.getInstance().getPage(name, pageAttributes);

            return wikiPage;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
