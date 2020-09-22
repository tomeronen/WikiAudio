package com.example.wikiaudio.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.Wikipage;
import com.example.wikiaudio.wikipedia.Wikipage;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Wikipage}.
 */
public class PlaylistRecyclerViewAdapter
        extends RecyclerView.Adapter<PlaylistRecyclerViewAdapter.WikiPageViewHolder> {

    private List<Wikipage> mWikipages;

    public PlaylistRecyclerViewAdapter(Playlist playlist) {
        mWikipages = playlist.getWikipages();
    }

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
        holder.descriptionView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if(mWikipages != null) {
            return mWikipages.size();
        }
        return 0;
    }


    public class WikiPageViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView titleView;
        public final TextView descriptionView;
        public Wikipage mItem;
        private boolean expanded = false;

        public WikiPageViewHolder(View view) {
            super(view);
            mView = view;
            titleView = (TextView) view.findViewById(R.id.title_view);
            descriptionView = (TextView) view.findViewById(R.id.description_view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expanded = !expanded;
                    if(expanded && mItem.getDescription() != null)
                    {
                        descriptionView.setText(mItem.getDescription());
                        descriptionView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        descriptionView.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}