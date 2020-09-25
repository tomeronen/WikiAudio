package com.example.wikiaudio.activates.mediaplayer.ui;

import android.app.Activity;
import android.content.Intent;
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
import com.example.wikiaudio.activates.MainActivity;
import com.example.wikiaudio.activates.choose_categories.ChooseCategoriesActivity;
import com.example.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.example.wikiaudio.activates.search_page.SearchPageActivity;
import com.ohoussein.playpause.PlayPauseView;

/**
 * The lower bar used as the wikipages player AND user navigator.
 */
public class MediaPlayerFragment extends Fragment {
    private static final String TAG = "MediaPlayerFragment";
    private static final float READING_SPEED = 1f;
    public static final int CHOOSE_CATEGORY_TAG = 1072;

    //Vars
    private View fragmentInflated;
    private FragmentActivity fragmentActivity;
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
        fragmentInflated = inflater.inflate(R.layout.fragment_media_player, container, false);
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

        fragmentActivity = this.getActivity();
        if (fragmentActivity == null) {
            Log.d(TAG, "initVars: null activity error");
        }
        playButton.change(false);
    }

    /**
     * Connects the fragment to the audio player logic
     */
    public void setAudioPlayer(MediaPlayer mediaPlayer) {
        player = mediaPlayer;
        if (player == null) {
            Log.d(TAG, "setAudioPlayer: got null audioPlayer");
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
                playButton.change(true);
            } else {
                player.playCurrent();
                playButton.change(false);
            }
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
            Activity activeActivity = getActiveActivity();
            if (activeActivity == null) {
                Log.d(TAG, "homeButton.setOnClickListener: got null activeActivity");
                return;
            }
            //if we're in MainActivity, we change nothing
            if (activeActivity.getLocalClassName().equals("activates.MainActivity")) {
                Log.d(TAG, "homeButton.setOnClickListener: we're on MainActivity");
            } else {
                // ow, redirects to the main activity AND clears all running intents
                Intent main = new Intent(player.getActivity(), MainActivity.class);
                main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(main);
            }
        });

        searchButton.setOnClickListener(v -> {
            // redirect to SearchActivity
            Activity activeActivity = getActiveActivity();
            if (activeActivity == null) {
                Log.d(TAG, "homeButton.setOnClickListener: got null activeActivity");
                return;
            }
            //if we're in SearchPageActivity, we change nothing
            if (activeActivity.getLocalClassName().equals("activates.search_page.SearchPageActivity")) {
                Log.d(TAG, "homeButton.setOnClickListener: we're on SearchPageActivity");
            } else {
                // ow, redirects to the SearchPageActivity
                Intent searchPageIntent  = new Intent(player.getActivity(), SearchPageActivity.class);
                startActivityForResult(searchPageIntent, CHOOSE_CATEGORY_TAG);
//                startActivity(searchPageIntent);
            }
        });

        categoriesButton.setOnClickListener(v -> {
            // redirect to ChooseCategoriesActivity
            Activity activeActivity = getActiveActivity();
            if (activeActivity == null) {
                Log.d(TAG, "homeButton.setOnClickListener: got null activeActivity");
                return;
            }
            //if we're in ChooseCategoriesActivity, we change nothing
            if (activeActivity.getLocalClassName().equals("activates.choose_categories.ChooseCategoriesActivity")) {
                Log.d(TAG, "homeButton.setOnClickListener: we're on ChooseCategoriesActivity");
            } else {
                // ow, redirects to the ChooseCategoriesActivity
                Intent chooseCategories = new Intent(player.getActivity(), ChooseCategoriesActivity.class);
                startActivity(chooseCategories);
            }
        });
    }

    private Activity getActiveActivity() {
        if (player == null || player.getAppData() == null ||
                player.getAppData().getWikiAudioApp() == null) {
            Log.d(TAG, "getActiveActivity: got null player/appdata/wikisudioapp");
            return null;
        }
        return player.getAppData().getWikiAudioApp().getActiveActivity();
    }

}