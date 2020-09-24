package com.example.wikiaudio.activates.mediaplayer.mediaplayer_ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.wikiaudio.R;
import com.ohoussein.playpause.PlayPauseView;

import java.util.Locale;

public class MediaPlayerFragment extends Fragment {

    private static final String TAG = "MediaPlayerFragment";
    private static final float READING_SPEED = 1f;

    //Vars
    private View fragmentInflated;
    private FragmentActivity activity;
    private WikipediaPlayer player;

    //idk
    private boolean showPlayingData;

    //Views
    private ImageButton previousButton;
    private PlayPauseView playButton;
    private ImageButton nextButton;
    private TextView wikipageTitle;
    private TextView playlistTitle;
    private ImageButton homeButton;
    private ImageButton searchButton;
    private ImageButton categoriesButton;

    public MediaPlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentInflated = inflater.inflate(R.layout.fragment_media_player1, container, false);
        initVars();
        setOnClickButtonsForPlayer();
        setOnClickButtonsForNavigationBar();
        return fragmentInflated;
    }



    private void initVars() {
        previousButton = fragmentInflated.findViewById(R.id.previousButton);
        playButton = fragmentInflated.findViewById(R.id.playPauseButton);
        nextButton = fragmentInflated.findViewById(R.id.nextButton);
        wikipageTitle = fragmentInflated.findViewById(R.id.wikipageTitle);
        playlistTitle = fragmentInflated.findViewById(R.id.playlistTitle);
        homeButton = fragmentInflated.findViewById(R.id.homeButton);
        searchButton = fragmentInflated.findViewById(R.id.searchButton);
        categoriesButton = fragmentInflated.findViewById(R.id.categoriesButton);

        activity = this.getActivity();
        if (activity == null) {
            Log.d(TAG, "initVars: null activity error");
        }
        player = new WikipediaPlayer(activity, Locale.ENGLISH, READING_SPEED); // should be an input TODO
    }

    private void setOnClickButtonsForPlayer() {
        previousButton.setOnClickListener(v -> {
//                // previous song logic:
//                int curPosition = getAppData().getCurPosition();
//                Playlist playlist = getAppData().getPlaylist();
//                if(playlist != null
//                        && curPosition > 0 // if we are in zero do nothing
//                        && playlist.size() >= curPosition)
//                {
//                    --curPosition; // go back one step.
//                    getAppData().setCurPositionInPlaylist(curPosition);
//                    title.setText(playlist.get(curPosition).getTitle());
//                    if(getAppData().getPlayingStatus()) // if we were playing, stop it.
//                    {
//                        player.stopPlaying();
//                    }
//                    else
//                    {
//                        playButton.toggle();
//                    }
//                    player.playWiki(playlist.get(curPosition));
//                    getAppData().setPlayingStatus(true);
//                }
        });

        playButton.setOnClickListener(v -> {
//                int curPosition = getAppData().getCurPosition();
//                Playlist playlist = getAppData().getPlaylist();
//                boolean currentlyPlaying = appData.getPlayingStatus();
//                playButton.toggle();
//                if(!canPlay(curPosition, playlist))
//                {
//                    playButton.toggle(); // doing toggle and return if needed makes cool animation.
//                }
//                if (currentlyPlaying) {
//                    pausePlaying();
//                    getAppData().setPlayingStatus(false);
//                }
//                // already playing -> pause.
//                else {
//                    startPlaying();
//                    getAppData().setPlayingStatus(true);
//                }
        });

        nextButton.setOnClickListener(v -> {
//            int curPosition = getAppData().getCurPosition();
//            Playlist playlist = getAppData().getPlaylist();
//            boolean playingStatus = appData.getPlayingStatus();
//            if(playlist != null
//                    && curPosition < (playlist.size() - 1)){
//                ++curPosition;
//                getAppData().setCurPositionInPlaylist(curPosition);
//                title.setText(playlist.get(curPosition).getTitle());
//                player.playWiki(playlist.get(curPosition));
//                if(!playingStatus)
//                {
//                    // we were not playing.
//                    playButton.toggle();
//                }
//                getAppData().setPlayingStatus(true);
//            }
        });
    }

    private void setOnClickButtonsForNavigationBar() {
        homeButton.setOnClickListener(v -> {
            // redirect to MainActivity
        });

        searchButton.setOnClickListener(v -> {
            // redirect to SearchActivity
        });

        categoriesButton.setOnClickListener(v -> {
            // redirect to ChooseCategoriesActivity
        });
    }

}
