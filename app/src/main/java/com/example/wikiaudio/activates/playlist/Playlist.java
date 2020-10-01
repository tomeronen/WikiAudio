package com.example.wikiaudio.activates.playlist;

import android.util.Log;

import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.activates.playlist.playlist_ui.PlaylistFragment;
import com.example.wikiaudio.wikipedia.wikipage.PageAttributes;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;
import com.example.wikiaudio.wikipedia.server.WorkerListener;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private static final String TAG = "Playlist";
    private static final String NEARBY_PLAYLIST_TITLE = "Nearby";
    private static final int RADIUS = 10000;
    private static final int MAX_WIKIPAGES = 10;

    private String title;
    private Playlist currentPlaylist;

    private List<Wikipage> wikipages = new ArrayList<>();
    private ArrayList<PageAttributes> pageAttributes = new ArrayList<>();
    private PlaylistFragment playlistFragment;

    // Location related
    private boolean isLocationBased;
    private double lat;
    private double lon;

    public Playlist() {}

    public Playlist(String title) {
        this.title = title;
    }

    public Playlist(Wikipage wikipage) {
        if (wikipage == null) {
            Log.d(TAG, "Playlist: got null wikipage");
            return;
        }
        this.title = "";
        wikipages.add(wikipage);
    }


    /**
     * Creates a playlist of wikipages that belong to the given category
     */
    public Playlist(String category, final boolean isLocationBased, double lat, double lon) {
        this.title = category;
        initVars(isLocationBased, lat, lon);

        List<Wikipage> results = new ArrayList<>();
        Holder.wikipedia.loadSpokenPagesByCategories(category, pageAttributes, results,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Playlist: loadSpokenPagesNamesByCategories-onSuccess");
                        if (results.size() > MAX_WIKIPAGES) {
                            for (int index = 0; index < MAX_WIKIPAGES; index++) {
                                Wikipage wikipage = results.get(index);
                                wikipages.add(wikipage);
                                wikipage.setPlaylist(currentPlaylist);
                            }
                        } else {
                            for(Wikipage wikipage: results) {
                                wikipages.add(wikipage);
                                wikipage.setPlaylist(currentPlaylist);
                            }
                        }
                        playlistFragment.notifyAdapter();
                    }

                    @Override
                    public void onFailure() {
                        Log.d(TAG, "Playlist: loadSpokenPagesNamesByCategories-onFailure");
                    }
                });
    }

    //todo documentation? can use initVars for attributes...
    public Playlist(String query, String action) {
        if(action.equals("search")) {
            this.title = query;
            List<PageAttributes> pageAttributes = new ArrayList<>();
            pageAttributes.add(PageAttributes.title);
            pageAttributes.add(PageAttributes.url);
            pageAttributes.add(PageAttributes.thumbnail);
            pageAttributes.add(PageAttributes.description);
            this.playlistFragment = new PlaylistFragment(this);
            Holder.wikipedia.searchForPage(query, pageAttributes, this.getWikipages(),
                    new WorkerListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "");
                            playlistFragment.notifyAdapter();
                        }

                        @Override
                        public void onFailure() {
                            Log.d(TAG, "");

                        }
                    });
        }
    }

    /**
     * Creates a playlist of wikipages that are nearby the given location
     */
    public Playlist(final boolean isLocationBased, double lat, double lon) {
        this.title = NEARBY_PLAYLIST_TITLE;
        initVars(isLocationBased, lat, lon);

        List<Wikipage> results = new ArrayList<>();
        Holder.wikipedia.getPagesNearby(lat, lon, RADIUS, results, pageAttributes,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG,"Playlist-WorkerListener-onSuccess: we found pages nearby!");

                        if (results.size() > MAX_WIKIPAGES) {
                            for (int index = 0; index < MAX_WIKIPAGES; index++) {
                                Wikipage wikipage = results.get(index);
                                wikipages.add(wikipage);
                                wikipage.setPlaylist(currentPlaylist);
                                Holder.locationHandler.markLocation(wikipage);
                            }
                        } else {
                            for(Wikipage wikipage: results) {
                                wikipages.add(wikipage);
                                wikipage.setPlaylist(currentPlaylist);
                                Holder.locationHandler.markLocation(wikipage);
                            }
                        }
                        playlistFragment.notifyAdapter();
                    }

                    @Override
                    public void onFailure() {
                        Log.d(TAG,"Playlist WorkerListener onFailure: couldn't find pages nearby");
                    }
                });
    }

    private void initVars(final boolean isLocationBased, double lat, double lon) {
        currentPlaylist = this;
        this.isLocationBased = isLocationBased;
        this.lat = lat;
        this.lon = lon;
        this.playlistFragment = new PlaylistFragment(this);
        pageAttributes.add(PageAttributes.title);
        pageAttributes.add(PageAttributes.url);
        pageAttributes.add(PageAttributes.coordinates);
        pageAttributes.add(PageAttributes.thumbnail);
        pageAttributes.add(PageAttributes.audioUrl);
        pageAttributes.add(PageAttributes.description);
    }

    /**
     * When we load wikipages by category we only get their title
     * So we use this func to get the wikipage itself (with the defined attributes)
     */
    private void loadWikipageByTitle(String title) {
        if (Holder.wikipedia == null) {
            Log.d(TAG, "getWikipageByTitle: error, wikipedia object is null");
        }
        final Wikipage result = new Wikipage();
        Holder.wikipedia.getWikipage(title, pageAttributes, result,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "getWikipageByTitle-searchForPage-WorkerListener-onSuccess");
                        // TODO Add location related condition - now it adds regardless
                        if (wikipages.size() < MAX_WIKIPAGES) {
                            wikipages.add(result);
                            result.setPlaylist(currentPlaylist);
                        }
                        playlistFragment.notifyAdapter();
                    }
                    @Override
                    public void onFailure() {
                        Log.d(TAG, "getWikipageByTitle-searchForPage-WorkerListener-onFailure: something went wrong");
                    }
                });
    }

    public List<Wikipage> getWikipages(){
        return wikipages;
    }

    public String getTitle() {
        return title;
    }

    public PlaylistFragment getPlaylistFragment() {
        return playlistFragment;
    }

    public Wikipage get(int position){
        if(position >= 0 && position < wikipages.size())
        {
            return wikipages.get(position);
        }
        return null;
    }

    public int size() {
        return wikipages.size();
    }

    public boolean isEmpty() {
        return wikipages.isEmpty();
    }

    public int getIndexByWikipage(Wikipage wikipage) {
        return wikipages.indexOf(wikipage);
    }

    public Wikipage getWikipageByIndex(int index) {
        return wikipages.get(index);
    }

    public void setPlaylistFragment(PlaylistFragment playlistFragment) {
        this.playlistFragment = playlistFragment;
    }

    public void addWikipage(Wikipage wikipage) {
        wikipages.add(wikipage);
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}