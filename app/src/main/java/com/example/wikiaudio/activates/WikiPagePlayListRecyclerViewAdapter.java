package com.example.wikiaudio.activates;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.WikiPage;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link WikiPage}.
 */
public class WikiPagePlayListRecyclerViewAdapter
        extends RecyclerView.Adapter<WikiPagePlayListRecyclerViewAdapter.WikiPageViewHolder> {

    private List<WikiPage> mValues = new ArrayList<>();
    public WikiPagePlayListRecyclerViewAdapter(List<WikiPage> items) {
        mValues = items;
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
        holder.mContentView.setText(mValues.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public class WikiPageViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public WikiPage mItem;

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