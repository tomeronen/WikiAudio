package com.example.wikiaudio.activates.mediaplayer.ui;

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
import com.example.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.ohoussein.playpause.PlayPauseView;

/**
 * The lower bar used as the wikipages player AND user navigator.
 */
public class MediaPlayerFragment extends Fragment {
    private static final String TAG = "MediaPlayerFragment";
    private static final float READING_SPEED = 1f;

    //Vars
    private View fragmentInflated;
    private FragmentActivity activity;
    private MediaPlayer player;

    //Views
    private ImageButton previousButton;
    private PlayPauseView playButton;
    private ImageButton nextButton;
    private TextView wikipageTitleView;
    private TextView playlistTitleView;
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
        wikipageTitleView = fragmentInflated.findViewById(R.id.wikipageTitle);
        playlistTitleView = fragmentInflated.findViewById(R.id.playlistTitle);
        homeButton = fragmentInflated.findViewById(R.id.homeButton);
        searchButton = fragmentInflated.findViewById(R.id.searchButton);
        categoriesButton = fragmentInflated.findViewById(R.id.categoriesButton);

        activity = this.getActivity();
        if (activity == null) {
            Log.d(TAG, "initVars: null activity error");
        }
    }

    /**
     * Connects the fragment to the audio player logic
     */
    public void setAudioPlayer(MediaPlayer mediaPlayer) {
        player = mediaPlayer;
        if (player == null) {
            Log.d(TAG, "setAudioPlayer: got null audioPlayer");
        } else {
            playButton.change(player.getIsPlaying());
        }
    }

    public void updateWhatIsPlayingTitles(String playlistTitle, String wikipageTitle) {
        playlistTitleView.setText(playlistTitle);
        wikipageTitleView.setText(wikipageTitle);
    }

    private void setOnClickButtonsForPlayer() {
        previousButton.setOnClickListener(v -> {
            if (player == null) {
                Log.d(TAG, "setOnClickButtonsForPlayer: null player");
                return;
            }
            player.playPrevious();
            playButton.change(!player.getIsPlaying());
        });

        playButton.setOnClickListener(v -> {
            if (player == null) {
                Log.d(TAG, "setOnClickButtonsForPlayer: null player");
                return;
            }
            if (player.getIsPlaying()) {
                player.pause();
            } else {
                player.playCurrent();
            }
            playButton.change(!player.getIsPlaying());
            Log.d(TAG, "setOnClickButtonsForPlayer: is playing? " + player.getIsPlaying());
        });

        nextButton.setOnClickListener(v -> {
            if (player == null) {
                Log.d(TAG, "setOnClickButtonsForPlayer: null player");
                return;
            }
            player.playNext();
            playButton.change(!player.getIsPlaying());
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
