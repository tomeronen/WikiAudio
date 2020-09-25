package com.example.wikiaudio.activates.playlist.playlist_ui;

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
import com.example.wikiaudio.activates.playlist.Playlist;

/**
 * A fragment for displaying the wikipages playlist
 */
public class PlaylistFragment extends Fragment {
    private static final String TAG = "PlaylistFragment";

    private Playlist playlist;
    public WikipagePlaylistRecyclerViewAdapter wikipagePlayListRecyclerViewAdapter;
    private boolean setBorder = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlaylistFragment() {}

    public PlaylistFragment(Playlist playlist) {
        this.playlist = playlist;
        wikipagePlayListRecyclerViewAdapter =
                new WikipagePlaylistRecyclerViewAdapter(playlist);
    }

//    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wikipage_item_list, viewGroup, false);

        if(setBorder) {
            view.setBackground(getActivity().getDrawable(R.drawable.upper_and_lower_border));
            view.setPadding(0,8,0,8);
        }

        //Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            DividerItemDecoration dividerItemDecoration
                    = new DividerItemDecoration(recyclerView.getContext(),
                    linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(wikipagePlayListRecyclerViewAdapter);
        }
        return view;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void notifyAdapter() {
        wikipagePlayListRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void highlightWikipage(int position) {
        wikipagePlayListRecyclerViewAdapter.highlightWikipage(position);
    }

    public void clearHighlights() {
        wikipagePlayListRecyclerViewAdapter.clearHighlights();
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        playlist.setPlaylistFragment(this);
    }

    public void showBorder(boolean b) {
        this.setBorder = b;
    }
}