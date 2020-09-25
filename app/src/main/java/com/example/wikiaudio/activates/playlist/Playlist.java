package com.example.wikiaudio.activates.playlist;

import android.util.Log;

import com.example.wikiaudio.Holder;
import com.example.wikiaudio.activates.playlist.playlist_ui.PlaylistFragment;
import com.example.wikiaudio.wikipedia.wikipage.PageAttributes;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;
import com.example.wikiaudio.wikipedia.server.WorkerListener;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private static final String TAG = "Playlist";
    private static final String NEARBY_PLAYLIST_TITLE = "Nearby";
    private static final int RADIUS = 10000; // todo let user choose?
    private static final int MAX_WIKIPAGES = 10;

    private String title;
    private Playlist currentPlaylist;

    private List<Wikipage> wikipages = new ArrayList<>();
    private ArrayList<PageAttributes> pageAttributes = new ArrayList<>();
    PlaylistFragment playlistFragment;

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
     * Creates a playlist of wikipages that belong to the given category and if the playlist is
     * location based then the wikipages are also nearby the given location
     */
    public Playlist(String category, final boolean isLocationBased, double lat, double lon) {
        this.title = category;
        this.playlistFragment = new PlaylistFragment(this);
        initVars(isLocationBased, lat, lon);
        final List<String> titles = new ArrayList<>();
        // todo preferably replace this with a func that returns wikipages, not strings, if possible
        Holder.wikipedia.loadSpokenPagesNamesByCategories(category, titles,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Playlist: loadSpokenPagesNamesByCategories-onSuccess");
                        // todo option B. (S.M) what to choose?
//                        for (String title : titles) {
//                            loadWikipageByTitle(title);
//                        }
                        loadWikipagesByTitles(titles);
                        playlistFragment.notifyAdapter();
                    }
                    @Override
                    public void onFailure() {
                        Log.d(TAG, "Playlist: loadSpokenPagesNamesByCategories-onFailure");
                    }
                });
    }

    /**
     * gets all wikipages at once. (faster at total time.)
     * @param titles names of all wikipages to bring.
     */
    private void loadWikipagesByTitles(List<String> titles) {
        if (Holder.wikipedia == null) {
            Log.d(TAG, "getWikipageByTitle: error, wikipedia object is null");
        }
        final List<Wikipage> result = new ArrayList<>() ;
        Holder.wikipedia.getWikipagesByName(titles, pageAttributes, result,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "getWikipageByTitle-searchForPage-WorkerListener-onSuccess");
                        // TODO Add location related condition - now it adds regardless
                        wikipages.addAll(result);
                        for(Wikipage wikipage:result) // todo S.M
                        {
                            wikipage.setPlaylist(currentPlaylist);
                        }
                        playlistFragment.notifyAdapter();
                    }
                    @Override
                    public void onFailure() {
                        Log.d(TAG, "getWikipageByTitle-searchForPage-WorkerListener-onFailure: something went wrong");
                    }
                });
    }

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
        this.playlistFragment = new PlaylistFragment(this);
        initVars(isLocationBased, lat, lon);
        Holder.wikipedia.getPagesNearby(lat, lon, RADIUS, wikipages, pageAttributes,
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
                        // TODO: we also mark the nearby playlist whenever it is created
                        for (Wikipage wikipage: wikipages) {
                            wikipage.setPlaylist(currentPlaylist);
                            Holder.locationHandler.markLocation(wikipage);
                        }
                        playlistFragment.notifyAdapter();
                    }
                    @Override
                    public void onFailure() {
                        Log.d(TAG,"Playlist-WorkerListener-onFailure: couldn't find pages nearby");
                    }
                });
    }

    private void initVars(final boolean isLocationBased, double lat, double lon) {
        this.isLocationBased = isLocationBased;
        this.lat = lat;
        this.lon = lon;
        currentPlaylist = this;
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
                        if (wikipages.size() < MAX_WIKIPAGES)
                            wikipages.add(result);
                            result.setPlaylist(currentPlaylist);
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
}