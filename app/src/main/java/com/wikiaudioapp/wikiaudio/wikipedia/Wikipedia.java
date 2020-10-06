package com.wikiaudioapp.wikiaudio.wikipedia;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.wikiaudioapp.wikiaudio.WikiAudioApp;
import com.wikiaudioapp.wikiaudio.data.AppData;
import com.wikiaudioapp.wikiaudio.wikipedia.workers.UploadFileWorker;
import com.wikiaudioapp.wikiaudio.wikipedia.server.WikiServerHolder;
import com.wikiaudioapp.wikiaudio.wikipedia.server.WikiUserData;
import com.wikiaudioapp.wikiaudio.wikipedia.server.WikipageDataManager;
import com.wikiaudioapp.wikiaudio.wikipedia.server.WorkerListener;
import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.PageAttributes;
import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.Wikipage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Facade singleton class to all interaction with wikipedia.
 * Main purpose is to manage requests to wikipedia and decide what is the best method
 * thread/AsyncTask/workManager.
 */
public class Wikipedia {

    private static final int DAYS_BETWEEN_LOADING_CATEGORIES = 7;
    private static final int ATTEMPTS_BEFORE_FAILURE = 3;  // todo check what is best number
    private ExecutorService threadPool;
    private WikipageDataManager wikipageDataManager;
    private AppCompatActivity activ;
    AppData appData;
    List<WikiUserData> availableUsersData;

    public Wikipedia(AppCompatActivity activity) {
        initVars(activity);
    }

    private void initVars(AppCompatActivity activity) {
        activ = activity;
        threadPool =((WikiAudioApp)activity.getApplication()).getExecutorService();
        appData = ((WikiAudioApp) this.activ.getApplication()).getAppData();
        wikipageDataManager = new WikipageDataManager();
        initUsersData();
        initSpokenPagesMetaData();
    }

    /**
     * to make the app run faster, we start with looking on the pages with spoken audio and save
     * there audio file name.
     */
    private void initSpokenPagesMetaData() {
        threadPool.execute(()-> {
                int numberOfTries = 0;
                while (numberOfTries < ATTEMPTS_BEFORE_FAILURE)
                {
                    try
                    {
                        wikipageDataManager.initWikipageDataManager();
                        return;
                    } catch (IOException e) {
                        Log.e("internet attempt status:",
                                "attempt failure number:" + numberOfTries);
                        numberOfTries++;
                    }
                }
                // todo what to do if wikipageDataManager fails to init?
        });
    }

    /**
     * inits the array of current available wiki user data. when we try to do operations that
     * needs authentication we will try using one of the following users data.
     */
    private void initUsersData() {
        availableUsersData = new ArrayList<>();

        // this is my main account. have permission to load files. todo make more
        availableUsersData.add(new WikiUserData( "tomer ronen",
                "xTGHTibZAL3cBws",
                false));

        // future user names that still don't have permission to load files:
        //        String BotName = "Tomer207";
        //        String Password = "TomerRonen@9k7g4f8bhfmd5g1ukdan8rkr4idlgvc3";
        //        String Password = "WikiAudio@tkpemajv20jm4t1ofm2amr5mb7p1v9cv";
        //        String BotName  = "Tomer_ronen";
        //        String userName = "Tomer207";
        //        String password = "X94A2wgzHA36MQ2";
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
                              final List<Wikipage> listToFill,
                              final WorkerListener workerListener)
    {
        threadPool.execute(() -> {
            int numberOfTries = 0;
            while (numberOfTries < ATTEMPTS_BEFORE_FAILURE)
            {
                try
                {
                    listToFill
                            .addAll(wikipageDataManager
                                    .searchWikiPageByName(pageName,
                                                            attributes));
                    activ.runOnUiThread(workerListener::onSuccess);
                    return;
                } catch (IOException e) {
                    Log.e("internet attempt status:",
                            "attempt failure number:" + numberOfTries);
                    numberOfTries++;
                }
            }
            activ.runOnUiThread(workerListener::onFailure);
        });
    }

    /**
     * gets Wikipages nearby the given coordinates. if successful,
     * fills the 'listToFill' with found pages. And then runs workerListener.
     * @param latitude the latitude to preform the search on.
     * @param longitude the longitude to preform the search on.
     * @param radius the radius from coordinates to search in.
     * @param listToFill the Wikipage list to be filled with results.
     * @param pageAttributes the attributes to get on each wiki page found.
     * @param workerListener what to do if task fails or is successful.
     */
    public void getPagesNearby(final double latitude,
                               final double longitude,
                               final int radius,
                               final List<Wikipage> listToFill,
                               final List<PageAttributes> pageAttributes,
                               final WorkerListener workerListener) {
        threadPool.execute(() -> {
            int numberOfTries = 0;
            while (numberOfTries < ATTEMPTS_BEFORE_FAILURE) {
                try {
                    // task was successful.
                    List<Wikipage> pagesNearby = wikipageDataManager
                            .searchPagesNearby(latitude,
                                    longitude,
                                    radius,
                                    pageAttributes);
                    listToFill.addAll(pagesNearby);
                    activ.runOnUiThread(workerListener::onSuccess);
                    return;
                } catch (IOException e) {
                    Log.e("internet attempt status:",
                            "attempt failure number:" + numberOfTries);
                    numberOfTries++;
                }
            }
            activ.runOnUiThread(workerListener::onFailure);
        });
    }


    /**
     * loads the current spoken pages categories from wikipedia.
     */
    public void loadSpokenPagesCategories(final List<String> listToFill,
                                          final WorkerListener workerListener) {
        if (needToLoadCategories()) { // need to reload Categories -> load from wiki.
            threadPool.execute(() -> {
                int numberOfTries = 0;
                while (numberOfTries < ATTEMPTS_BEFORE_FAILURE) {
                    try {
                        listToFill.addAll(wikipageDataManager.getSpokenCategories());
                        listToFill
                                .addAll(WikiServerHolder.getInstance().callGetSpokenPagesCategories());
                        Date currentTime = Calendar.getInstance().getTime();
                        appData.setCategories(listToFill);
                        appData.setLastLoadedCategories(currentTime);
                        activ.runOnUiThread(workerListener::onSuccess);
                        return;
                    } catch (IOException e) {
                        Log.e("internet attempt status:",
                                "attempt failure number:" + numberOfTries);
                        numberOfTries++;
                    }
                }
                activ.runOnUiThread(workerListener::onFailure);
            });

        } else { // do not need to load Categories -> load from saved.
            List<String> categories = appData.getCategories();
            if(categories != null) {
                listToFill.addAll(appData.getCategories());
                workerListener.onSuccess();
            }
            else {
                workerListener.onFailure();
            }

        }
    }


    /**
     * checks if we need to reload Categories.
     * @return true if we need to reload or false if not.
     */
    private boolean needToLoadCategories() {
        Date currentTime = Calendar.getInstance().getTime();
        Date lastLoadedCategories = appData.getLastLoadedCategories();
        if(lastLoadedCategories != null) {
            long diffInMillies = Math.abs(currentTime.getTime() - lastLoadedCategories.getTime());
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            return  diffInDays > DAYS_BETWEEN_LOADING_CATEGORIES;
        }
        else {
            return true;
        }
    }

    /**
     * get a Wikipage by its name/title.
     * @param name the name of wiki page to get.
     * @param pageAttributes the Attributes to get on the page.
     * @param pageToFill the wiki page to fill with the data.
     * @param workerListener what to do if task fails or is successful.
     */
    public void getWikipage(final String name,
                            final List<PageAttributes> pageAttributes,
                            final Wikipage pageToFill,
                            final WorkerListener workerListener)
    {
        threadPool.execute(() -> {
            int numberOfTries = 0;
            while (numberOfTries < ATTEMPTS_BEFORE_FAILURE) {
                try {
                    pageToFill.copy(wikipageDataManager.getWikipage(name, pageAttributes));
                    activ.runOnUiThread(workerListener::onSuccess);
                    return;
                } catch (IOException e) {
                    Log.e("internet attempt status:",
                            "attempt failure number:" + numberOfTries);
                    numberOfTries++;
                }
            }
            activ.runOnUiThread(workerListener::onFailure);
        });
    }


    /**
     * get a list of wikipages by name.
     * notice! onSuccess runs after all process is finished.  onFailure runs each time bringing
     * one of the pages failed.
     * @param names the names of the wikipages to bring.
     * @param pageAttributes the attributes to get on each page.
     * @param listToFill the list to fill with the results.
     * @param workerListener callback when finished.
     */
    public void getWikipagesByName(final List<String> names,
                                   final List<PageAttributes> pageAttributes,
                                   final List<Wikipage> listToFill,
                                   final WorkerListener workerListener) {
        threadPool.execute(() -> {
            int numberOfTries = 0;
            while (numberOfTries < ATTEMPTS_BEFORE_FAILURE) {
                try {
                    List<Wikipage> pages = WikiServerHolder
                            .getInstance().getPagesByName(names, pageAttributes);
                    listToFill.addAll(pages);
                    activ.runOnUiThread(workerListener::onSuccess);
                    return;
                } catch (IOException e) {
                    Log.e("internet attempt status:",
                            "attempt failure number:" + numberOfTries);
                    numberOfTries++;
                }
            }
            activ.runOnUiThread(workerListener::onFailure);
        });
    }


    /**
     * upload a file to wikipedia.
     * @param fileName name of file to be uploaded.
     * @param filePath path to file to be uploaded.
     */
    public void uploadFile(final String fileName, final String filePath) {
        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(UploadFileWorker.class)
                        .setInputData(
                                new Data.Builder()
                                        .putString(UploadFileWorker.FILE_NAME_TAG, fileName)
                                        .putString(UploadFileWorker.FILE_PATH_TAG, filePath)
                                        .build()
                        )
                        .build();
        WorkManager
                .getInstance(activ)
                .enqueue(uploadWorkRequest);
    }

//todo finish implement.

//    public void login(String a, String b) {
//        WikiServerHolder.getInstance().callLogin(a,b);
//    }


    /**
     * load Spoken Pages By Categories.
     * @param category the category to bring the pages from.
     * @param result the list to add the pages to.
     * @param pageAttributes what Attributes to bring on result pages.
     * @param workerListener what to do if work fails or is successful.
     */
    public void loadSpokenPagesByCategories(final String category,
                                                 final List<PageAttributes> pageAttributes,
                                                 final List<Wikipage> result,
                                                 final WorkerListener workerListener)
    {

        threadPool.execute(() -> {
            int numberOfTries = 0;
            while (numberOfTries < ATTEMPTS_BEFORE_FAILURE) {
                try {
                    List<Wikipage> spokenPages = this.wikipageDataManager
                            .getSpokenPagesByCategories(category, pageAttributes);
                    result.addAll(spokenPages);
                    activ.runOnUiThread(workerListener::onSuccess);
                    return;
                } catch (IOException e) {
                    Log.e("internet attempt status:",
                            "attempt failure number:" + numberOfTries);
                    numberOfTries++;
                }
            }
            activ.runOnUiThread(workerListener::onFailure);
        });
    }


    /**
     * @return a list of users data.
     */
    public List<WikiUserData> getUsersData() {
        return availableUsersData;
    }
}