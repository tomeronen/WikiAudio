package com.wikiaudioapp.wikiaudio.data;

import com.wikiaudioapp.wikiaudio.activates.playlist.Playlist;
import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.Wikipage;

/**
 * An object that holds every relevant data for what is being played
 * (playlist, wikipage, index & if it is currently playing or not).
 */
public class CurrentlyPlayed {
    private Playlist playlist = null;
    private Wikipage wikipage = null;
    private int index = -1;
    private boolean isPlaying = false;

    public CurrentlyPlayed() {}

    public CurrentlyPlayed(Playlist playlist, Wikipage wikipage, int index, boolean isPlaying) {
        this.playlist = playlist;
        this.wikipage = wikipage;
        this.index = index;
        this.isPlaying = isPlaying;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist cPlaylist) {
        this.playlist = cPlaylist;
    }

    public Wikipage getWikipage() {
        return wikipage;
    }

    public void setWikipage(Wikipage cWikipage) {
        this.wikipage = cWikipage;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int cIndex) {
        this.index = cIndex;
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isValid(){
        return (playlist != null && wikipage != null && index > -1);
    }
}
