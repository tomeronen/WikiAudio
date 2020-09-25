package com.example.wikiaudio.activates.playlist.playlist_ui;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.R;
import com.example.wikiaudio.activates.WikipageActivity;
import com.example.wikiaudio.activates.playlist.Playlist;
import com.example.wikiaudio.activates.playlist.PlaylistsManager;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class WikipagePlaylistRecyclerViewAdapter extends
        RecyclerView.Adapter<WikipagePlaylistRecyclerViewAdapter.WikiPageViewHolder> {
    private static final String TAG = "WikipagePlaylistRecyclerViewAdapter";

    private Playlist playlist;
    private List<Wikipage> mValues;
    private List<WikiPageViewHolder> wikiPageViewHolders = new ArrayList<>();

    public WikipagePlaylistRecyclerViewAdapter(Playlist playlist) {
        this.playlist = playlist;
        if (playlist != null) {
            mValues = playlist.getWikipages();
        }
    }

    @NonNull
    @Override
    public WikiPageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wikipage_item, parent, false);
        WikiPageViewHolder wikiPageViewHolder = new WikiPageViewHolder(view);
        wikiPageViewHolders.add(wikiPageViewHolder);
        return wikiPageViewHolder;
    }

    @Override
    public void onBindViewHolder(final WikiPageViewHolder holder, int position) {
        Wikipage wikipage = mValues.get(position);
        holder.position = position;
        holder.wikipage = wikipage;
        holder.titleView.setText(wikipage.getTitle());
        holder.descriptionView.setText(wikipage.getDescription());
        holder.descriptionView.setVisibility(View.GONE); // we start without seeing content.
        holder.highlight.setVisibility(View.GONE);

        //Shows and sets the location button if that wikipage has coordinates
        if (wikipage.getLat() == null || wikipage.getLon() == null) {
            holder.locationButton.setVisibility(View.GONE);
        } else {
            holder.locationButton.setOnClickListener(v ->
                    Holder.locationHandler.markAndZoom(wikipage));
        }

        if (Holder.playlistsManager != null && Holder.playlistsManager.getMediaPlayer() != null) {
            holder.playButton.setOnClickListener(v ->
                    Holder.playlistsManager.getMediaPlayer().play(playlist, position));
        }
    }

    @Override
    public int getItemCount() {
        if(mValues != null)
            return mValues.size();
        return 0;
    }

    public void highlightWikipage(int position) {
        for (WikiPageViewHolder wikiPageViewHolder: wikiPageViewHolders) {
            if (wikiPageViewHolder.position == position) {
                wikiPageViewHolder.highlight.setVisibility( View.VISIBLE);
            } else {
                wikiPageViewHolder.highlight.setVisibility( View.GONE);
            }
        }
    }

    public void clearHighlights() {
        for (WikiPageViewHolder wikiPageViewHolder: wikiPageViewHolders) {
            wikiPageViewHolder.highlight.setVisibility( View.GONE);
        }
    }


    public class WikiPageViewHolder extends RecyclerView.ViewHolder {
        // Views
        public final View mView;
        public final TextView titleView;
        public final TextView descriptionView;
        public TextView highlight;
        private FloatingActionButton locationButton;
        private FloatingActionButton playButton;


        public Wikipage wikipage;
        public int position;
        private boolean expanded = false;

        public WikiPageViewHolder(View view) {
            super(view);
            mView = view;
            titleView = view.findViewById(R.id.title_view);
            descriptionView = view.findViewById(R.id.description_view);
            locationButton = view.findViewById(R.id.locationButton);
            playButton = view.findViewById(R.id.playButton);
            highlight = view.findViewById(R.id.highlight);

            view.setOnClickListener(v -> {
                expanded = !expanded;
                if (expanded && wikipage.getDescription() != null) {
                    descriptionView.setText(wikipage.getDescription());
                    descriptionView.setVisibility(View.VISIBLE);
                } else {
                    descriptionView.setVisibility(View.GONE);
                }
            });

            // When long clicking on an item in the playlist, it opens its wikipage
            view.setOnLongClickListener((View.OnLongClickListener) v -> {
                if (wikipage != null && playlist != null) {
                    int index = playlist.getIndexByWikipage(wikipage);
                    if (index > -1) {
                        Intent WikipageIntent = new Intent(PlaylistsManager.getActivity(), WikipageActivity.class);
                        WikipageIntent.putExtra("playlistTitle", playlist.getTitle());
                        WikipageIntent.putExtra("index", index);
                        PlaylistsManager.getActivity().startActivity(WikipageIntent);
                    } else {
                        Log.d(TAG, "onInfoWindowClick: index is bad");
                    }
                } else {
                    Log.d(TAG, "onInfoWindowClick: playlist is null :(");
                }

            return false;
            });
        }
    }
}