package com.example.wikiaudio.activates.mediaplayer;

import android.app.Activity;
import android.util.Log;

import com.example.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.example.wikiaudio.activates.playlist.Playlist;
import com.example.wikiaudio.data.AppData;
import com.example.wikiaudio.data.CurrentlyPlayed;
import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

public class MediaPlayer {
    private static final String TAG = "MediaPlayer";

    private Activity activity;
    private WikipagePlayer player;
    private AppData appData;
    private MediaPlayerFragment mpFragment;

    private CurrentlyPlayed currentlyPlayed;
    private boolean isPlaying = false;


    public MediaPlayer(Activity activity, AppData appData, MediaPlayerFragment mediaPlayerFragment) {
        this.activity = activity;
        this.appData = appData;
        mpFragment = mediaPlayerFragment;
        player = Holder.wikipagePlayer;
        checkForActivePlaylist();
    }

    public void checkForActivePlaylist() {
        CurrentlyPlayed currentlyPlayed = appData.getCurrentlyPlayed();
        if (currentlyPlayed != null && currentlyPlayed.isValid() &&
                currentlyPlayed.getIsPlaying()) {
            this.currentlyPlayed = currentlyPlayed;
            isPlaying = true;
            displayWhatIsBeingPlayed(null); //todo maybe null is not the answer
        } else {
            Log.d(TAG, "checkForActivePlaylist: currentlyPlayed is null");
        }
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void pauseForActivityChange() {
        Log.d(TAG, "pauseForActivityChange: :)");
    }

    public void pause() {
        player.pausePlaying();
        appData.getCurrentlyPlayed().setIsPlaying(false);
        isPlaying = false;
    }

    public void play(Playlist playlist, int index) {
        if (playlist == null || index < 0 || playlist.getWikipageByIndex(index) == null) {
            Log.d(TAG, "play: null playlist/wikipage or bad index");
            return;
        }
        Playlist previousPlaylist = null;
        if (currentlyPlayed != null && currentlyPlayed.isValid()
                && currentlyPlayed.getPlaylist() != playlist) {
            previousPlaylist = currentlyPlayed.getPlaylist();
        }

        if (isPlaying) {
            isPlaying = false;
            pause();
        }

        Wikipage wikipage = playlist.getWikipageByIndex(index);
        isPlaying = player.playWikipage(wikipage);
        updateMediaPlayerVars(playlist, index, wikipage);
        displayWhatIsBeingPlayed(previousPlaylist);
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

    private void updateMediaPlayerVars(Playlist playlist, int index, Wikipage wikipage) {
        currentlyPlayed = new CurrentlyPlayed(playlist, wikipage, index, true);
        if (appData != null) {
            appData.setCurrentlyPlayed(currentlyPlayed);
        } else {
            Log.d(TAG, "updateMediaPlayerVars: got null appData");
        }
    }

    private void displayWhatIsBeingPlayed(Playlist previousPlaylist) {
        if (currentlyPlayed == null || !currentlyPlayed.isValid()) {
            Log.d(TAG, "displayWhatIsBeingPlayed: currentlyPlayed is null, nothing to display");
            return;
        }
        Playlist playlist = currentlyPlayed.getPlaylist();
        Wikipage wikipage = currentlyPlayed.getWikipage();
        int index = currentlyPlayed.getIndex();

        // map zoom in on wikipage
        if (wikipage.getLat() != null && wikipage.getLon() != null) {
            Holder.locationHandler.markAndZoom(wikipage);
        }
        // highlight the wikipage item on the playlist tab
        if (playlist.getPlaylistFragment() != null) {
            playlist.getPlaylistFragment().highlightWikipage(index);
        }
        // remove all highlights from the previous playlist
        if (previousPlaylist != null && previousPlaylist.getPlaylistFragment() != null) {
            previousPlaylist.getPlaylistFragment().clearHighlights();
        }
        // display on the media player fragment
        if (mpFragment != null) {
            mpFragment.togglePlayPauseButton(false);
            mpFragment.updateWhatIsPlayingTitles(playlist.getTitle(), wikipage.getTitle());
        } else {
            Log.d(TAG, "updateMediaPlayerVars: got null mpFragment");
        }
    }

    public Playlist getCurrentPlaylist() {
        if (currentlyPlayed.isValid()) {
            return currentlyPlayed.getPlaylist();
        }
        return null;
    }

    public Wikipage getCurrentWikipage() {
        if (currentlyPlayed.isValid()) {
            return currentlyPlayed.getWikipage();
        }
        return null;
    }

    public CurrentlyPlayed getCurrentlyPlayed() {
        return currentlyPlayed;
    }
}
