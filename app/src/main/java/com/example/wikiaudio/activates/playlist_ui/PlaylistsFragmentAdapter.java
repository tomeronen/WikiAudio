package com.example.wikiaudio.activates.playlist_ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragmentAdapter extends FragmentStatePagerAdapter {

    private final List<PlaylistFragment> mFragmentList = new ArrayList<>();

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
}
