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

public class PlaylistsFragmentAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "PlaylistsFragmentAdapter";

    private List<PlaylistFragment> mFragmentList = new ArrayList<>();
    private TabLayout tabs;

    public PlaylistsFragmentAdapter(FragmentManager manager) {
        super(manager);
    }

    @NonNull
    @Override
    public PlaylistFragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public int getItemPosition(Object item) {
        PlaylistFragment fragment = (PlaylistFragment)item;
        int position = mFragmentList.indexOf(fragment);
        if (position >= 0) {
            return position;
        } else {
            return POSITION_NONE;
        }

    }

    public void updatePlaylistFragmentList() {
        resetFragmentList();
        for (Playlist playlist: PlaylistsManager.getPlaylists()) {
            Log.d(TAG, "updatePlaylistFragmentList: add the fragment of playlist titled - " + playlist.getTitle());
            addPlaylistFragment(playlist.getPlaylistFragment());
            playlist.getPlaylistFragment().setPlaylistsFragmentAdapter(this);
        }
    }

    public void addPlaylistFragment(PlaylistFragment fragment) {
        if (fragment != null && fragment.getPlaylist() != null &&
                fragment.getPlaylist().getTitle().equals("Nearby")) {
            mFragmentList.add(0, fragment);
        } else {
            mFragmentList.add(fragment);
        }
    }

    public void removePlaylistFragment(int position) {
        if (!mFragmentList.isEmpty() && position > -1 && position < mFragmentList.size()) {
            mFragmentList.remove(position);
            notifyDataSetChanged();
        }
    }

    private void resetFragmentList() {
        mFragmentList = new ArrayList<>();
    }

    public void setTabs(TabLayout tabs) {
        this.tabs = tabs;
    }

    public TabLayout getTabs() {
        return tabs;
    }

}
