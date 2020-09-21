package com.example.wikiaudio.playlist;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragmentAdapter extends FragmentPagerAdapter {

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
}
