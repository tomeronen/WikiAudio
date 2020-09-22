package com.example.wikiaudio.activates.playlist_ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.Handler;
import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.Wikipage;

import java.util.List;

public class WikipagePlaylistRecyclerViewAdapter extends
        RecyclerView.Adapter<WikipagePlaylistRecyclerViewAdapter.WikiPageViewHolder> {

    private List<Wikipage> mValues;

    public WikipagePlaylistRecyclerViewAdapter(List<Wikipage> wikipages) {
        mValues = wikipages;
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
        // update wiki page view holder: todo add what else we want to do.
        holder.mItem = mValues.get(position);
        holder.titleView.setText(mValues.get(position).getTitle());
        holder.descriptionView.setText(mValues.get(position).getDescription());
        holder.descriptionView.setVisibility(View.GONE); // we start without seeing content.
        if (mValues.get(position).getLat() == null || mValues.get(position).getLon() == null) {
            holder.locationButton.setVisibility(View.GONE);
        } else {
            holder.locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Handler.locationHandler.markAndZoom(mValues.get(position));
                }
            });
        }
        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo add playing this wikipage
            }
        });
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