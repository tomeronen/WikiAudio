package com.example.wikiaudio.activates.playlist.playlist_ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.Holder;
import com.example.wikiaudio.R;
import com.example.wikiaudio.activates.playlist.Playlist;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

import java.util.List;

public class WikipagePlaylistRecyclerViewAdapter extends
        RecyclerView.Adapter<WikipagePlaylistRecyclerViewAdapter.WikiPageViewHolder> {

    private Playlist playlist;
    private List<Wikipage> mValues;

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

        return new WikiPageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final WikiPageViewHolder holder, int position) {
        Wikipage wikipage = mValues.get(position);
        holder.mItem = wikipage;
        holder.titleView.setText(wikipage.getTitle());
        holder.descriptionView.setText(wikipage.getDescription());
        holder.descriptionView.setVisibility(View.GONE); // we start without seeing content.
        if (wikipage.getLat() == null || wikipage.getLon() == null) {
            holder.locationButton.setVisibility(View.GONE);
        } else {
            holder.locationButton.setOnClickListener(v ->
                    Holder.locationHandler.markAndZoom(wikipage));
        }
        holder.playButton.setOnClickListener(v ->
                Holder.playlistsManager.getMediaPlayer().play(playlist, position));
    }

    @Override
    public int getItemCount() {
        if(mValues != null)
            return mValues.size();
        return 0;
    }


    public class WikiPageViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView titleView;
        public final TextView descriptionView;
        private ImageButton locationButton;
        private ImageButton playButton;
        public Wikipage mItem;
        private boolean expanded = false;

        public WikiPageViewHolder(View view) {
            super(view);
            mView = view;
            titleView = view.findViewById(R.id.title_view);
            descriptionView = view.findViewById(R.id.description_view);
            locationButton = view.findViewById(R.id.locationButton);
            playButton = view.findViewById(R.id.playButton);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expanded = !expanded;
                    if(expanded && mItem.getDescription() != null) {
                        descriptionView.setText(mItem.getDescription());
                        descriptionView.setVisibility(View.VISIBLE);
                    } else {
                        descriptionView.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}