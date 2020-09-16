package com.example.wikiaudio.activates;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlayListsFragmentAdapter extends FragmentPagerAdapter {

    private final List<PlayListFragment> mFragmentList = new ArrayList<>();

    public PlayListsFragmentAdapter(FragmentManager manager) {
        super(manager);
    }

    @NonNull
    @Override
    public PlayListFragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }


    public void addFrag(PlayListFragment fragment) {
        mFragmentList.add(fragment);
    }
}
