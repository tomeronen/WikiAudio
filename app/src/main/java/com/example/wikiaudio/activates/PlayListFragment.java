package com.example.wikiaudio.activates;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.Wikipage;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class PlayListFragment extends Fragment{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private List<Wikipage> contents;
    private WikiPagePlayListRecyclerViewAdapter playListRecyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayListFragment() {
    }


    public PlayListFragment(List<Wikipage> contents) {
        this.contents = contents;
        playListRecyclerViewAdapter
                = new WikiPagePlayListRecyclerViewAdapter(contents);
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static PlayListFragment newInstance() {
        PlayListFragment fragment = new PlayListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wikipage_item_list,
                                                        container,
                                                    false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            DividerItemDecoration dividerItemDecoration
                    = new DividerItemDecoration(recyclerView.getContext(),
                                                    linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(playListRecyclerViewAdapter);
        }
        return view;
    }

    public List<Wikipage> getPlayList() {
        return contents;
    }

    public void notifyAdapter() {
        playListRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void update(){
        this.notifyAdapter();
    }
}