package com.example.wikiaudio.activates.mediaplayer;

import android.app.Activity;
import android.util.Log;

import com.example.wikiaudio.AppData;
import com.example.wikiaudio.Holder;
import com.example.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.example.wikiaudio.activates.playlist.Playlist;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

import java.util.Locale;

public class MediaPlayer {
    private static final String TAG = "AudioPlayer";
    private static final float READING_SPEED = 1f;

    private WikipagePlayer player;
    private MediaPlayerFragment mpFragment;
    private AppData appData;
    private Activity activity;

    private boolean isPlaying = false;
    private Playlist currentPlaylist;
    private Wikipage currentWikipage;
    private int currentPositionInPlaying;

    public MediaPlayer(Activity activity, AppData appData, MediaPlayerFragment mediaPlayerFragment) {
        player = new WikipagePlayer(activity, Locale.ENGLISH, READING_SPEED); //todo might have an issue with the activity input
        mpFragment = mediaPlayerFragment;
        this.appData = appData;
        this.activity = activity;
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void pause() {
        player.pausePlaying();
        isPlaying = false;
    }

    public void play(Playlist playlist, int index) {
        if (playlist == null || index < 0 || playlist.getWikipageByIndex(index) == null) {
            Log.d(TAG, "play: null playlist/wikipage or bad index");
            return;
        }
        Wikipage wikipage = playlist.getWikipageByIndex(index);
        if (isPlaying) {
            isPlaying = false;
            pause();
        }
        player.playWiki(wikipage);
        isPlaying = true;

        // zoom in on wikipage
        if (wikipage.getLat() != null && wikipage.getLon() != null) {
            Holder.locationHandler.markAndZoom(wikipage);
        }

        // highlight the wikipage on the playlist
        if (playlist.getPlaylistFragment() != null) {
            playlist.getPlaylistFragment().highlightWikipage(index);
        }

        // if it's a new playlist, then remove all highlights from the previous one
        if (currentPlaylist != null && playlist != currentPlaylist && currentPlaylist.getPlaylistFragment() != null) {
            currentPlaylist.getPlaylistFragment().clearHighlights();
        }

        //Update
        mpFragment.updateWhatIsPlayingTitles(playlist.getTitle(), wikipage.getTitle());
        setCurrentPlaylist(playlist);
        setCurrentWikipage(wikipage);
        setCurrentPositionInPlaying(index);
    }

    public void playCurrent() {
        if (isPlaying) {
            return;
        }
        if (currentWikipage != null) {
            play(currentPlaylist, currentPositionInPlaying);
            //todo resume from minute x - not  a must
        } else {
            Log.d(TAG, "playCurrent: can't play current null wikipage :)");
        }
    }

    public void playPrevious() {
        if (!isCurrentValid()) {
            Log.d(TAG, "playPrevious: current wikipage/playlist/index is null/invalid");
            return;
        }
        if (currentPositionInPlaying < 1) {
            Log.d(TAG, "playPrevious: there's no previous wikipage for the first one :)");
            return;
        }
        play(currentPlaylist, currentPositionInPlaying - 1);
    }

    public void playNext() {
        if (!isCurrentValid()) {
            Log.d(TAG, "playPrevious: current wikipage/playlist/index is null/invalid");
            return;
        }
        if (currentPositionInPlaying >= currentPlaylist.size() - 1) {
            Log.d(TAG, "playPrevious: there's no next wikipage for the last one :)");
            return;
        }
        play(currentPlaylist, currentPositionInPlaying + 1);
    }

    private boolean isCurrentValid() {
        return (currentWikipage != null &&  currentPlaylist != null && currentPositionInPlaying >= 0);
    }

    private void setCurrentPlaylist(Playlist playlist) {
        currentPlaylist = playlist;
    }

    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    private void setCurrentWikipage(Wikipage wikipage) {
        currentWikipage = wikipage;
    }

    public Wikipage getCurrentWikipage() {
        return currentWikipage;
    }

    private void setCurrentPositionInPlaying(int position) {
        currentPositionInPlaying = position;
    }

    public int getCurrentPositionInPlaying() {
        return currentPositionInPlaying;
    }

    public AppData getAppData() {
        return appData;
    }

    public Activity getActivity() {
        return activity;
    }

}
