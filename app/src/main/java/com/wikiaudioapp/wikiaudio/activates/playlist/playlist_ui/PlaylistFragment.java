package com.wikiaudioapp.wikiaudio.activates.playlist.playlist_ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiaudioapp.wikiaudio.R;
import com.wikiaudioapp.wikiaudio.activates.playlist.Playlist;

/**
 * A fragment for displaying the wikipages playlist
 */
public class PlaylistFragment extends Fragment {
    //For logs
    private static final String TAG = "PlaylistFragment";

    private Playlist playlist;
    private PlaylistsFragmentAdapter playlistsFragmentAdapter;
    public WikipagePlaylistRecyclerViewAdapter wikipagePlayListRecyclerViewAdapter;
    private boolean setBorder = false;


    private RecyclerView recyclerView;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
            recyclerView = (RecyclerView) view;
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

    public void setPlaylistsFragmentAdapter(PlaylistsFragmentAdapter playlistsFragmentAdapter) {
        this.playlistsFragmentAdapter = playlistsFragmentAdapter;
    }

    public PlaylistsFragmentAdapter getPlaylistsFragmentAdapter() {
        return playlistsFragmentAdapter;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

}
