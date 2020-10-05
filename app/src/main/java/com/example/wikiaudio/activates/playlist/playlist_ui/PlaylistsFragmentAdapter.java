package com.example.wikiaudio.activates.playlist.playlist_ui;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.wikiaudio.activates.playlist.Playlist;
import com.example.wikiaudio.activates.playlist.PlaylistsManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * The Adapter to browse the different PlaylistFragments
 */
public class PlaylistsFragmentAdapter extends FragmentStatePagerAdapter {
    //For logs
    private static final String TAG = "PlaylistsFragmentAdapter";

    private List<PlaylistFragment> playlistFragments = new ArrayList<>();
    private TabLayout tabLayout;

    public PlaylistsFragmentAdapter(FragmentManager manager) {
        super(manager);
    }

    @NonNull
    @Override
    public PlaylistFragment getItem(int position) {
        return playlistFragments.get(position);
    }

    @Override
    public int getCount() {
        return playlistFragments.size();
    }

    @Override
    public int getItemPosition(@NonNull Object item) {
        PlaylistFragment fragment = (PlaylistFragment)item;
        int position = playlistFragments.indexOf(fragment);
        if (position >= 0) {
            return position;
        } else {
            return POSITION_NONE;
        }
    }

    /**
     * Clears existing list and adds PlaylistFragments for every existing playlist
     */
    public void updatePlaylistFragmentList() {
        playlistFragments = new ArrayList<>();
        for (Playlist playlist: PlaylistsManager.getPlaylists()) {
            Log.d(TAG, "updatePlaylistFragmentList: add the fragment of playlist titled - " + playlist.getTitle());
            PlaylistFragment playlistFragment = playlist.getPlaylistFragment();
            if(playlistFragment != null)
            {
                addPlaylistFragment(playlistFragment);
                playlistFragment.setPlaylistsFragmentAdapter(this);
            }

        }
    }

    /**
     * Adds the given fragment to the list; if it's the Nearby playlist's fragment then adds it
     * in the beginning
     */
    public void addPlaylistFragment(PlaylistFragment fragment) {
        if (fragment != null && fragment.getPlaylist() != null &&
                fragment.getPlaylist().getTitle().equals("Nearby")) {
            playlistFragments.add(0, fragment);
        } else {
            playlistFragments.add(fragment);
        }
    }

    public void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    public TabLayout getTabLayout() {
        return tabLayout;
    }

}
