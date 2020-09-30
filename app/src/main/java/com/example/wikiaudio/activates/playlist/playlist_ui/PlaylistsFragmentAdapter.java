package com.example.wikiaudio.activates.playlist.playlist_ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragmentAdapter extends FragmentStatePagerAdapter {

    private final List<PlaylistFragment> mFragmentList = new ArrayList<>();
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

    public void addPlaylistFragment(PlaylistFragment fragment) {
        mFragmentList.add(fragment);
    }

    public void refreshAllFragments() {
        for (PlaylistFragment playlistFragment: mFragmentList)
            playlistFragment.notifyAdapter();
    }

    public void setTabs(TabLayout tabs) {
        this.tabs = tabs;
    }

    public TabLayout getTabs() {
        return tabs;
    }
}
