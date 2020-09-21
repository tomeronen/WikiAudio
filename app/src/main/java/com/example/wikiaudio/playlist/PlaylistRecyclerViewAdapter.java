package com.example.wikiaudio.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.Wikipage;

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
        holder.mItem = mWikipages.get(position);
        holder.mContentView.setText(mWikipages.get(position).getTitle());
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
        public final TextView mContentView;
        public Wikipage mItem;

        public WikiPageViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}