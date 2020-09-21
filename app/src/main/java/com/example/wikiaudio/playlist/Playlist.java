package com.example.wikiaudio.playlist;

import android.util.Log;

import com.example.wikiaudio.Handler;
import com.example.wikiaudio.wikipedia.PageAttributes;
import com.example.wikiaudio.wikipedia.Wikipage;
import com.example.wikiaudio.wikipedia.WorkerListener;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private static final String TAG = "Playlist";
    private static final String NEARBY_PLAYLIST_TITLE = "Nearby";
    private static final int RADIUS = 10000; // todo let user choose?
    private static final int MAX_WIKIPAGES = 10;

    private String title;
    private boolean shouldBeMarked = false;

    private List<Wikipage> wikipages = new ArrayList<>();
    private ArrayList<PageAttributes> pageAttributes = new ArrayList<>();

    // Location related
    private boolean isLocationBased;
    private double lat;
    private double lon;
    private String wikipageTitle;


    public Playlist() {}

    /**
     * Creates a playlist of wikipages that belong to the given category and if the playlist is
     * location based then the wikipages are also nearby the given location
     */
    public Playlist(String category, final boolean isLocationBased, double lat, double lon) {
        this.title = category;
        initVars(isLocationBased, lat, lon);
        final List<String> titles = new ArrayList<>();
        // todo preferably replace this with a func that returns wikipages, not strings, if possible
        Handler.wikipedia.loadSpokenPagesNamesByCategories(category, titles,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Playlist: loadSpokenPagesNamesByCategories-onSuccess");
                        for (String title : titles) {
                            loadWikipageByTitle(title);
                        }
                    }
                    @Override
                    public void onFailure() {
                        Log.d(TAG, "Playlist: loadSpokenPagesNamesByCategories-onFailure");
                    }
                });
    }

//    /**
//     * Create a playlist of wikipages that belong to the given categories and if nearby is true,
//     * then are also nearby the user's current location
//     * @param categories
//     * @param nearby
//     */
//    public Playlist(WArrayList<String> categories, boolean nearby) {
//    }

    /**
     * Creates a playlist of wikipages that are nearby the given location
     */
    public Playlist(final boolean isLocationBased, double lat, double lon) {
        this.title = NEARBY_PLAYLIST_TITLE;
        initVars(isLocationBased, lat, lon);
        Handler.wikipedia.getPagesNearby(lat, lon, RADIUS, wikipages, pageAttributes,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG,"Playlist-WorkerListener-onSuccess: we found pages nearby!");
                        if (wikipages.size() > MAX_WIKIPAGES) {
                            List<Wikipage> lessWikipages = new ArrayList<>();
                            for (int i = 0; i < MAX_WIKIPAGES; i++) {
                                lessWikipages.add(wikipages.get(i));
                            }
                            wikipages = lessWikipages;
                        }
                        // TODO this means we also mark the nearby playlist when it is created
                        for (Wikipage wikipage: wikipages)
                            Handler.locationHandler.markLocation(wikipage);
                    }
                    @Override
                    public void onFailure() {
                        Log.d(TAG,"Playlist-WorkerListener-onFailure: couldn't find pages nearby");
                    }
                });
    }

    /**
     * Pretty self-explanatory, really.
     */
    private void initVars(final boolean isLocationBased, double lat, double lon) {
        this.isLocationBased = isLocationBased;
        this.lat = lat;
        this.lon = lon;
        pageAttributes.add(PageAttributes.title);
        pageAttributes.add(PageAttributes.url);
        pageAttributes.add(PageAttributes.coordinates);
        pageAttributes.add(PageAttributes.thumbnail);
        pageAttributes.add(PageAttributes.audioUrl);
    }

    /**
     * Pretty self-explanatory, really.
     */
    private void loadWikipageByTitle(String title) {
        this.wikipageTitle = title;
        if (Handler.wikipedia == null) {
            Log.d(TAG, "getWikipageByTitle: error, wikipedia object is null");
        }
        final List<Wikipage> results = new ArrayList<>();
        Handler.wikipedia.searchForPage(title, pageAttributes, results,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "getWikipageByTitle-searchForPage-WorkerListener-onSuccess");
                        // TODO Add location related condition - now it adds regardless
                        if (wikipages.size() < MAX_WIKIPAGES)
                            wikipages.add(results.get(0));
                    }
                    @Override
                    public void onFailure() {
                        Log.d(TAG, "getWikipageByTitle-searchForPage-WorkerListener-onFailure: something went wrong");
                    }
                });
    }

    /**
     * Pretty self-explanatory, really.
     */
    public List<Wikipage> getWikipages(){
        return wikipages;
    }

    /**
     * Pretty self-explanatory, really.
     */
    public String getTitle() {
        return title;
    }
}
