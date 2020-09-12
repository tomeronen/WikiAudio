package com.example.wikiaudio.wikipedia;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
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

/**
 * Facade singleton class to all interaction with wikipedia.
 * Main purpose is to manage requests to wikipedia and decide what is the best method
 * thread/AsyncTask/workManager.
 */
public class Wikipedia {

    public ArrayList<String> spokenPagesCategories;
    private static Wikipedia instance = null;
    LinkedTreeMap<String, List<String>> spokenCategories;
    private Activity activ;

    public Wikipedia(Activity activity) {
        activ = activity;
    }

    /**
     * Search a name for relevant wiki pages.
     * Fills list to fill with relevant wiki pages with name and id data.
     * Does not handle bad writing.
     * (if you now the exact name of the page you want use `getPage()` ).
     * @param pageName the value to search.
     * @param attributes currently not in use. can be null.
     * @param listToFill the list to fill with result.
     * @param workerListener what to do if task fails or is successful.
     */
    public void searchForPage(final String pageName,
                           final List<PageAttributes> attributes,
                           final List<WikiPage> listToFill,
                           final WorkerListener workerListener)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listToFill.addAll(WikiServerHolder
                                                    .getInstance().searchPage(pageName));
                    activ.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            workerListener.onSuccess();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    activ.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            workerListener.onFailure();
                        }
                    });
                }

            }
        }).start();
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
                    List<WikiPage> pagesNearby = WikiServerHolder.getPagesNearby(latitude,
                            longitude,
                            radius,
                            pageAttributes);
                    // task was successful.
                    listToFill.addAll(pagesNearby);
                    activ.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            workerListener.onSuccess();
                        }
                    });
                } catch (IOException e) {
                    // task failed with a exception.
                    activ.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            workerListener.onFailure();
                        }
                    });
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
                                            final String category,
                                            List<PageAttributes> p,
                                            WorkerListener workerListener)
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


    /**
     *
     * @param name the name of wiki page to get.
     * @param pageAttributes the Attributes to get on the page.
     * @param pageToFill the wiki page to fill with the data.
     * @param workerListener what to do if task fails or is successful.
     */
    public void getWikiPage(final String name,
                            final List<PageAttributes> pageAttributes,
                            final WikiPage pageToFill,
                            final WorkerListener workerListener)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pageToFill.copy(WikiServerHolder.getInstance().getPage(name, pageAttributes));

                    activ.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            workerListener.onSuccess();
                        }
                    });
                } catch (IOException e) {
                    // task failed with a exception.
                    activ.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            workerListener.onFailure();
                        }
                    });
                }
            }
        }).start();
    }



//todo finish implement.

//    public void login(String a, String b) {
//        WikiServerHolder.getInstance().callLogin(a,b);
//    }
}
