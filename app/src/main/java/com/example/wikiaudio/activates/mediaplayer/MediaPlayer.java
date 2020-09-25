package com.example.wikiaudio.activates.mediaplayer;

import android.app.Activity;
import android.util.Log;

import com.example.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.example.wikiaudio.activates.playlist.Playlist;
import com.example.wikiaudio.data.AppData;
import com.example.wikiaudio.data.CurrentlyPlayed;
import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

import java.util.Locale;

public class MediaPlayer {
    private static final String TAG = "AudioPlayer";
    private static final float READING_SPEED = 1f;

    private WikipagePlayer player;
    private MediaPlayerFragment mpFragment;
    private AppData appData;
    private Activity activity;

    CurrentlyPlayed currentlyPlayed;
    private boolean isPlaying = false;


    public MediaPlayer(Activity activity, AppData appData, MediaPlayerFragment mediaPlayerFragment) {
        player = new WikipagePlayer(activity, Locale.ENGLISH, READING_SPEED); //todo might have an issue with the activity input
        mpFragment = mediaPlayerFragment;
        this.appData = appData;
        this.activity = activity;
        checkForActivePlaylist();
    }

    private void checkForActivePlaylist() {
        CurrentlyPlayed currentlyPlayed = appData.getCurrentlyPlayed();
        if (currentlyPlayed != null) {
            play(currentlyPlayed.getPlaylist(), currentlyPlayed.getIndex());
        } else {
            Log.d(TAG, "checkForActivePlaylist: currentlyPlayed is null");
        }
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void pauseForActivityChange() {
        player.pausePlaying();
    }

    public void pause() {
        player.pausePlaying();
        CurrentlyPlayed currentlyPlayed = appData.getCurrentlyPlayed();
        currentlyPlayed.setIsPlaying(false);
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

        updateMediaPlayerVars(playlist, index, wikipage);
        displayWhatIsBeingPlayed(playlist, index, wikipage);
    }

    public void playCurrent() {
        if (isPlaying) {
            return;
        }
        if (currentlyPlayed != null) {
            play(currentlyPlayed.getPlaylist(), currentlyPlayed.getIndex());
            //todo resume from minute x - not  a must
        } else {
            Log.d(TAG, "playCurrent: can't play current null wikipage :)");
        }
    }

    public void playPrevious() {
        if (!currentlyPlayed.isValid()) {
            Log.d(TAG, "playPrevious: current wikipage/playlist/index is null/invalid");
            return;
        }
        if (currentlyPlayed.getIndex() == 0) {
            Log.d(TAG, "playPrevious: there's no previous wikipage for the first one :)");
            return;
        }
        play(currentlyPlayed.getPlaylist(), currentlyPlayed.getIndex() - 1);
    }

    public void playNext() {
        if (!currentlyPlayed.isValid()) {
            Log.d(TAG, "playNext: current wikipage/playlist/index is null/invalid");
            return;
        }
        if (currentlyPlayed.getIndex() == currentlyPlayed.getPlaylist().size() - 1) {
            Log.d(TAG, "playNext: there's no next wikipage for the last one :)");
            return;
        }
        play(currentlyPlayed.getPlaylist(), currentlyPlayed.getIndex() + 1);
    }

    public AppData getAppData() {
        return appData;
    }

    public Activity getActivity() {
        return activity;
    }

    private void displayWhatIsBeingPlayed(Playlist playlist, int index, Wikipage wikipage) {
        // zoom in on wikipage
        if (wikipage.getLat() != null && wikipage.getLon() != null) {
            Holder.locationHandler.markAndZoom(wikipage);
        }
        // highlight the wikipage on the playlist
        if (playlist.getPlaylistFragment() != null) {
            playlist.getPlaylistFragment().highlightWikipage(index);
        }
        // if it's a new playlist, then remove all highlights from the previous one
        if (currentlyPlayed.isValid() && playlist != currentlyPlayed.getPlaylist() && currentlyPlayed.getPlaylist().getPlaylistFragment() != null) {
            currentlyPlayed.getPlaylist().getPlaylistFragment().clearHighlights();
        }
    }

    private void updateMediaPlayerVars(Playlist playlist, int index, Wikipage wikipage) {
        if (mpFragment != null) {
            mpFragment.updateWhatIsPlayingTitles(playlist.getTitle(), wikipage.getTitle());
        } else {
            Log.d(TAG, "updateMediaPlayerVars: got null mpFragment");
        }
        currentlyPlayed = new CurrentlyPlayed(playlist, wikipage, index, true);

        if (appData != null) {
            appData.setCurrentlyPlayed(currentlyPlayed);
        } else {
            Log.d(TAG, "updateMediaPlayerVars: got null appData");
        }
    }

    public Playlist getCurrentPlaylist() {
        if (currentlyPlayed.isValid()) {
            return currentlyPlayed.getPlaylist();
        }
        return null;
    }
}
