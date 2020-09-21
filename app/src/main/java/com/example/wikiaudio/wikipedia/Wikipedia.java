package com.example.wikiaudio.wikipedia;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.WikiAudioApp;
import com.google.gson.internal.LinkedTreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Facade singleton class to all interaction with wikipedia.
 * Main purpose is to manage requests to wikipedia and decide what is the best method
 * thread/AsyncTask/workManager.
 */
public class Wikipedia {

    private final ExecutorService threadPool;
    public ArrayList<String> spokenPagesCategories;
    private static Wikipedia instance = null;
    LinkedTreeMap<String, List<String>> spokenCategories;
    private AppCompatActivity activ;

    public Wikipedia(AppCompatActivity activity) {
        activ = activity;
        threadPool =((WikiAudioApp)activity.getApplication()).getExecutorService();
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
                               final WorkerListener workerListener)
    {
        threadPool.execute(() -> {
            try {
                List<Wikipage> pagesNearby = WikiServerHolder.getPagesNearby(latitude,
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
        });
    }


    /**
     * loads the current spoken pages categories from wikipedia.
     */
    public void loadSpokenPagesCategories(final List<String> listToFill,
                                          final WorkerListener workerListener)
    {
        threadPool.execute(() -> {
            try {
                listToFill
                        .addAll(WikiServerHolder.getInstance().callGetSpokenPagesCategories());
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


        });

        // worker option:

//        WorkRequest loadSpokenCategoriseWorkerReq =
//                new OneTimeWorkRequest
//                        .Builder(loadSpokenCategoriseWorker.class)
//                        .setInputData(new Data.Builder()
//                                .build())
//                        .build();
//        WorkManager.getInstance(activ).enqueue(loadSpokenCategoriseWorkerReq);
//        WorkManager.getInstance(activ)
//                .getWorkInfoByIdLiveData(loadSpokenCategoriseWorkerReq.getId())
//                .observe(activ, new Observer<WorkInfo>() {
//                    @Override
//                    public void onChanged(WorkInfo workInfo) {
//                        if (workInfo.getState() == WorkInfo.State.FAILED)
//                        {
//                            activ.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                         workerListener.onFailure();
//                                }
//                            });
//                        }
//                        else if (workInfo.getState() == WorkInfo.State.SUCCEEDED)
//                        {
//                            activ.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    workerListener.onSuccess();
//                                }
//                            });
//                        }
//                    }
//                });
    }

    /**
     *
     * @param category
     * @param result
     * @param workerListener
     */
    public void loadSpokenPagesNamesByCategories(final String category,
                                            final List<String> result,
                                            final WorkerListener workerListener)
    {
        try {
            List<String> pageNames = new ArrayList<>();
            String url = "https://en.wikipedia.org/wiki/Wikipedia:Spoken_articles";
            Document doc = null;
            doc = Jsoup.connect(url).get();
            Elements elements = doc.select(".mw-headline, li");
            for (int i = 1; i < elements.size(); i++) {
                Element curElement = elements.get(i);
                if ("mw-headline".equals(curElement.className()) && curElement.text().equals(category)) {
                    ++i;
                    curElement = elements.get(i);
                    while (i < elements.size() && !"mw-headline".equals(curElement.className())) {
                        result.add(curElement.text());
                        ++i;
                        curElement = elements.get(i);
                    }
                    break;
                }
            }
            activ.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    workerListener.onSuccess();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            workerListener.onFailure();
        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    result.addAll(WikiServerHolder.callGetSpokenPagesNamesByCategories(category));
//                    activ.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            workerListener.onSuccess();
//                        }
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    activ.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            workerListener.onFailure();
//                        }
//                    });
//                }
//            }
//        }).start();

//        option with worker:
//        WorkRequest loadSpokenCategoriseWorkerReq =
//                new OneTimeWorkRequest
//                        .Builder(loadSpokenPagesByCategoriseWorker.class)
//                        .setInputData(new Data.Builder()
//                        .putString(loadSpokenPagesByCategoriseWorker.categoryTag, category).build())
//                        .build();
//        WorkManager.getInstance(ownerActivity).enqueue(loadSpokenCategoriseWorkerReq);
//        // todo is activity a life cycle owner?
//        WorkManager.getInstance(ownerActivity)
//                .getWorkInfoByIdLiveData(loadSpokenCategoriseWorkerReq.getId())
//                .observe((LifecycleOwner) activ, new Observer<WorkInfo>() {
//                    @Override
//                    public void onChanged(WorkInfo workInfo) {
//                        if(workInfo.getState() == WorkInfo.State.SUCCEEDED)
//                        {
//                            activ.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    workerListener.onSuccess();
//                                }
//                            });
//                        }
//
//
//                    }
//                });
//        return loadSpokenCategoriseWorkerReq.getId();
    }


    /**
     *
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
    public void getWikipages(final List<String> names,
                            final List<PageAttributes> pageAttributes,
                            final List<Wikipage> listToFill,
                            final WorkerListener workerListener)
    {
        threadPool.execute(() -> {
            for (String name : names) {
                try {
                    listToFill.add(WikiServerHolder.getInstance().getPage(name, pageAttributes));
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
            activ.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    workerListener.onSuccess();
                }
            });
        });
    }


    /**
     * upload a file to wikipedia.
     * @param fileName name of file to be uploaded.
     * @param filePath path to file to be uploaded.
     */
    public void uploadFile(final String fileName, final String filePath) {
        threadPool.execute(() -> {
//                try {
//                    WikiServerHolder.getInstance().uploadFile(fileName, filePath);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
        });
    }

//todo finish implement.

//    public void login(String a, String b) {
//        WikiServerHolder.getInstance().callLogin(a,b);
//    }
}
