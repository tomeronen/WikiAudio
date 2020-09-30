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
    private PlaylistPlayer player;
    private AppData appData;
    private MediaPlayerFragment mpFragment;

    private CurrentlyPlayed currentlyPlayed;
    private boolean isPlaying = false;
    private boolean isPaused = false;


    public MediaPlayer(Activity activity, AppData appData, MediaPlayerFragment mediaPlayerFragment) {
        this.activity = activity;
        this.appData = appData;
        mpFragment = mediaPlayerFragment;
        player = Holder.playlistPlayer;
        checkForActivePlaylist();
    }

    public void checkForActivePlaylist() {
        CurrentlyPlayed currentlyPlayed = appData.getCurrentlyPlayed();
        if (currentlyPlayed != null && currentlyPlayed.isValid() &&
                currentlyPlayed.getIsPlaying()) {
            this.currentlyPlayed = currentlyPlayed;
            isPlaying = true;
            displayWhatIsBeingPlayed(null);
        } else {
            Log.d(TAG, "checkForActivePlaylist: currentlyPlayed is null");
        }
    }

    public void pause() {
        player.pausePlaying();
        isPaused = true;
//        appData.getCurrentlyPlayed().setIsPlaying(false); todo maybe add a paused func
    }

    public void resume() {
        player.resumePlaying();
        isPaused = false;
        isPlaying = true;
    }

    public void play(Playlist playlist, int index) {
        if (playlist == null || index < 0 || index >= playlist.size() ||
                playlist.getWikipageByIndex(index) == null) {
            Log.d(TAG, "play: null playlist/wikipage or bad index");
            return;
        }
        Playlist previousPlaylist = null;
        if (currentlyPlayed != null && currentlyPlayed.isValid()
                && currentlyPlayed.getPlaylist() != playlist) {
            previousPlaylist = currentlyPlayed.getPlaylist();
        }

        if (isPlaying) {
            player.stopPlayer();
            isPlaying = false;
        }

        isPlaying = player.playPlaylistFromIndex(playlist, index);
        isPaused = false;

        updateMediaPlayerVars(playlist, index, playlist.getWikipageByIndex(index));
        displayWhatIsBeingPlayed(previousPlaylist);
    }

    public void playCurrent() {
        if (isPlaying) {
            return;
        }
        if (currentlyPlayed != null) {
            player.resumePlaying();
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

    /**
     * Used for when the (PlaylistPlayer) player continues to its next wikipage on the playlist.
     */
    public void updateNextWikipage() {
        if (currentlyPlayed == null || !currentlyPlayed.isValid()) {
            Log.d(TAG, "updateNextWikipage: currentlyPlayed is null, nothing to display");
            return;
        }

        Playlist playlist = currentlyPlayed.getPlaylist();
        int index = currentlyPlayed.getIndex() + 1;
        if (index <= 0  || index >= playlist.size()) {
            Log.d(TAG, "updateNextWikipage: bad index");
            return;
        }
        Wikipage wikipage = playlist.getWikipageByIndex(index);

        updateMediaPlayerVars(playlist, index, wikipage);
        displayWhatIsBeingPlayed(null);
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

        // zoom in on wikipage (map)
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
        if (currentlyPlayed != null && currentlyPlayed.isValid()) {
            return currentlyPlayed.getPlaylist();
        }
        return null;
    }

    public AppData getAppData() {
        return appData;
    }

    public Activity getActivity() {
        return activity;
    }

    public Wikipage getCurrentWikipage() {
        if (currentlyPlayed != null && currentlyPlayed.isValid()) {
            return currentlyPlayed.getWikipage();
        }
        return null;
    }

    public CurrentlyPlayed getCurrentlyPlayed() {
        return currentlyPlayed;
    }

    public boolean getIsPaused() {
        return isPaused;
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

}
