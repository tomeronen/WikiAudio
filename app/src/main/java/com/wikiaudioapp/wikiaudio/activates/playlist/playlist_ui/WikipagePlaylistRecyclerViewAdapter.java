package com.wikiaudioapp.wikiaudio.activates.playlist.playlist_ui;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiaudioapp.wikiaudio.data.Holder;
import com.wikiaudioapp.wikiaudio.R;
import com.wikiaudioapp.wikiaudio.activates.WikipageActivity;
import com.wikiaudioapp.wikiaudio.activates.playlist.Playlist;
import com.wikiaudioapp.wikiaudio.activates.playlist.PlaylistsManager;
import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.Wikipage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * The adapter for the representation of a wikipage on a playlist.
 */
public class WikipagePlaylistRecyclerViewAdapter extends
        RecyclerView.Adapter<WikipagePlaylistRecyclerViewAdapter.WikiPageViewHolder> {
    //For logs
    private static final String TAG = "WikipagePlaylistRecyclerViewAdapter";

    //Vars
    private Playlist playlist;
    private List<Wikipage> wikipages;
    private List<WikiPageViewHolder> wikiPageViewHolders = new ArrayList<>();

    public WikipagePlaylistRecyclerViewAdapter(Playlist playlist) {
        this.playlist = playlist;
        if (playlist != null) {
            wikipages = playlist.getWikipages();
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
        Wikipage wikipage = wikipages.get(position);
        holder.position = position;
        holder.wikipage = wikipage;
        holder.titleView.setText(wikipage.getTitle());
        holder.titleView.setSelected(true); // for moving text if needed
//        holder.descriptionView.setVisibility(View.GONE); // we start without seeing content.
//        holder.highlight.setVisibility(View.GONE);

        //Shows and sets the location button if that wikipage has coordinates
        if (wikipage.getLat() == null || wikipage.getLon() == null) {
            holder.locationButton.setVisibility(View.GONE);
        } else {
            holder.locationButton.setOnClickListener(v ->
                    Holder.locationHandler.markAndZoom(wikipage));
        }

        //Play button
        if (Holder.playlistsManager != null && Holder.playlistsManager.getMediaPlayer() != null) {
            holder.playButton.setOnClickListener(v ->
                    Holder.playlistsManager.getMediaPlayer().play(playlist, position));
            // if the media player is playing this wikipage then highlight it
            if (Holder.playlistsManager.getMediaPlayer().getIsPlaying()) {
                if (Holder.playlistsManager.getMediaPlayer().getCurrentWikipage().getTitle().equals(wikipage.getTitle())) {
//                    holder.highlight.setVisibility(View.VISIBLE);
                    holder.mView
                            .setBackground
                                    (holder.mView.getResources()
                                            .getDrawable(R.drawable.border_highlight));
                }
            }
        }

        if(wikipage.getDescription() != null)
        {
            holder.descriptionView.setText(wikipage.getDescription());
            holder.descriptionView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if(wikipages != null)
            return wikipages.size();
        return 0;
    }

    /**
     * Highlights the wikipage (on the given position) with a green frame
     */
    public void highlightWikipage(int position) {
        for (WikiPageViewHolder wikiPageViewHolder: wikiPageViewHolders) {
            if (wikiPageViewHolder.position == position) {
//                wikiPageViewHolder.highlight.setVisibility( View.VISIBLE);
                wikiPageViewHolder.mView
                        .setBackground
                                (wikiPageViewHolder.mView.getResources()
                                        .getDrawable(R.drawable.border_highlight));
            } else {
                wikiPageViewHolder.mView.setBackgroundResource(0);
//                wikiPageViewHolder.highlight.setVisibility( View.GONE);
            }
        }
    }

    public void clearHighlights() {
        for (WikiPageViewHolder wikiPageViewHolder: wikiPageViewHolders) {
            wikiPageViewHolder.mView.setBackgroundResource(0);
//            wikiPageViewHolder.highlight.setVisibility( View.GONE);
        }
    }


    public class WikiPageViewHolder extends RecyclerView.ViewHolder {
        // Views
        public final View mView;
        public final TextView titleView;
        public final TextView descriptionView;
//        public TextView highlight;
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

//            highlight = view.findViewById(R.id.highlight);

            view.setOnClickListener(v -> {
                if (wikipage != null && playlist != null) {
                    int index = playlist.getIndexByWikipage(wikipage);
                    if (index > -1) {
                        Intent WikipageIntent = new Intent(PlaylistsManager.getActivity(),
                                WikipageActivity.class);
                        WikipageIntent.putExtra("playlistTitle", playlist.getTitle());
                        WikipageIntent.putExtra("index", index);
                        PlaylistsManager.getActivity().startActivity(WikipageIntent);
                    } else {
                        Log.d(TAG, "onInfoWindowClick: index is bad");
                    }
                } else {
                    Log.d(TAG, "onInfoWindowClick: playlist is null :(");
                }
            });

            // CHANGE
//                expanded = !expanded;
//                if (expanded && wikipage.getDescription() != null) {
//                    descriptionView.setText(wikipage.getDescription());
//                    descriptionView.setVisibility(View.VISIBLE);
//                } else {
//                    descriptionView.setVisibility(View.GONE);


            // When long clicking on an item in the playlist, it opens its wikipage
            view.setOnLongClickListener(v -> {
                if (wikipage != null && playlist != null) {
                    int index = playlist.getIndexByWikipage(wikipage);
                    if (index > -1) {
                        Intent WikipageIntent = new Intent(PlaylistsManager.getActivity(),
                                WikipageActivity.class);
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
