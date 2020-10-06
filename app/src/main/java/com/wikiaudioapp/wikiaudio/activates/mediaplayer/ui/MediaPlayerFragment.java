package com.wikiaudioapp.wikiaudio.activates.mediaplayer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.wikiaudioapp.wikiaudio.R;
import com.wikiaudioapp.wikiaudio.activates.MainActivity;
import com.wikiaudioapp.wikiaudio.activates.WikipageActivity;
import com.wikiaudioapp.wikiaudio.activates.choose_categories.ChooseCategoriesActivity;
import com.wikiaudioapp.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.wikiaudioapp.wikiaudio.activates.playlist.Playlist;
import com.wikiaudioapp.wikiaudio.activates.search_page.SearchPageActivity;
import com.wikiaudioapp.wikiaudio.data.CurrentlyPlayed;
import com.wikiaudioapp.wikiaudio.data.Holder;
import com.ohoussein.playpause.PlayPauseView;

import java.util.Objects;

/**
 * The lower bar used as the wikipages player AND user navigator.
 */
public class MediaPlayerFragment extends Fragment {
    private static final String TAG = "MediaPlayerFragment";
    public static final int CHOOSE_CATEGORY_TAG = 1072;

    //Vars
    private View view;
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

    public MediaPlayerFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_media_player, container, false);
        initVars();
        setOnClickButtonsForPlayer();
        setOnClickButtonsForNavigationBar();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = getActivity();
        if (activity != null)
        {
            String localClassName = activity.getLocalClassName();
            highlightCurrentChoice(localClassName);
        }
    }

    private void highlightCurrentChoice(String localClassName) {
        // lightup the current window in bottom navigation bar:
        switch (localClassName)
        {
            case "com.wikiaudioapp.wikiaudio.activates.MainActivity":
                homeButton.setColorFilter(getResources().getColor(R.color.chosenItem));
                break;
            case "com.wikiaudioapp.wikiaudio.activates.search_page.SearchPageActivity":
                searchButton.setColorFilter(getResources().getColor(R.color.chosenItem));
                break;
            case "com.wikiaudioapp.wikiaudio.activates.choose_categories.ChooseCategoriesActivity":
                categoriesButton.setColorFilter(getResources().getColor(R.color.chosenItem));
                break;
        }
    }

    private void initVars() {
        //Player
        previousButton = view.findViewById(R.id.previousButton);
        playButton = view.findViewById(R.id.playPauseButton);
        nextButton = view.findViewById(R.id.nextButton);
        playButton.change(false);

        //Titles
        wikipageTitleView = view.findViewById(R.id.wikipageTitle);
        playlistTitleView = view.findViewById(R.id.playlistTitle);

        //Navigation
        homeButton = view.findViewById(R.id.homeButton);
        searchButton = view.findViewById(R.id.searchButton);
        categoriesButton = view.findViewById(R.id.categoriesButton);
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

    /**
     * Updates the playlistTitleView & wikipageTitleView with what is being currently played
     */
    public void updateTitlesWithCurrentlyPlayed(String playlistTitle, String wikipageTitle) {
        if (playlistTitleView == null || wikipageTitleView == null) {
            Log.d(TAG, "updateWhatIsPlayingTitles: null TextViews");
            return;
        }
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
                if (player.getIsPaused()) {
                    player.resume();
                    playButton.change(false);
                } else {
                    player.pause();
                    playButton.change(true);
                }
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

        // clicking on the title of the wikipage redirects to the relevant WikipageActivity
        wikipageTitleView.setOnClickListener(v -> {
            if (player == null) {
                Log.d(TAG, "setOnClickButtonsForPlayer: null player");
                return;
            }
            CurrentlyPlayed currentlyPlayed = player.getCurrentlyPlayed();
            if (currentlyPlayed != null && currentlyPlayed.isValid()) {
                Intent WikipageIntent = new Intent(getActiveActivity(), WikipageActivity.class);
                WikipageIntent.putExtra("playlistTitle", currentlyPlayed.getPlaylist().getTitle());
                WikipageIntent.putExtra("index", currentlyPlayed.getIndex());
                startActivity(WikipageIntent);
            } else {
                Log.d(TAG, "setOnClickButtonsForPlayer: got null wikipage, nowhere to redirect");
            }
        });

        // clicking on the title of the playlist shows its tab
        playlistTitleView.setOnClickListener(v -> {
            if (player == null) {
                Log.d(TAG, "setOnClickButtonsForPlayer - playlistTitleView: null player");
                return;
            }
            CurrentlyPlayed currentlyPlayed = player.getCurrentlyPlayed();
            if (currentlyPlayed != null && currentlyPlayed.isValid() && currentlyPlayed.getPlaylist() != null) {
                Playlist playlist = currentlyPlayed.getPlaylist();
                int playlistIndex = Holder.playlistsManager.getIndexByPlaylist(playlist);
                // gets the TabLayout object and selects the current playlist
                if (playlistIndex > -1 && playlist.getPlaylistFragment() != null &&
                        playlist.getPlaylistFragment().getPlaylistsFragmentAdapter() != null &&
                        playlist.getPlaylistFragment().getPlaylistsFragmentAdapter().getTabLayout() != null ) {
                    Activity activeActivity = getActiveActivity();
                    if (activeActivity != null) {
                        activeActivity.runOnUiThread(() ->
                                Objects.requireNonNull(playlist.getPlaylistFragment()
                                        .getPlaylistsFragmentAdapter().getTabLayout().getTabAt(playlistIndex))
                                        .select());
                    }
                } else {
                    Log.d(TAG, "setOnClickButtonsForPlayer - playlistTitleView: got bad index");
                }
            } else {
                Log.d(TAG, "setOnClickButtonsForPlayer - playlistTitleView: got null playlist, nothing to show");
            }
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
                // ow, redirects to the main activity AND clear all running intents
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
                ((SearchPageActivity) getActiveActivity()).openSearchBar();
            } else {
                // ow, redirects to the SearchPageActivity
                Intent searchPageIntent  = new Intent(player.getActivity(), SearchPageActivity.class);
                startActivity(searchPageIntent);
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

    /**
     * Sets the play/pause button. If true, sets play; ow, sets pause.
     */
    public void setPlayPauseButton(boolean toggle) {
        if (playButton != null) {
            playButton.change(toggle);
        }
    }

}