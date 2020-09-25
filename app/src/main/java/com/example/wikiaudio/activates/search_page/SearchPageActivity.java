package com.example.wikiaudio.activates.search_page;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.R;
import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.example.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.example.wikiaudio.activates.playlist.Playlist;
import com.example.wikiaudio.activates.playlist.playlist_ui.PlaylistFragment;
import com.example.wikiaudio.data.AppData;
import com.example.wikiaudio.data.Holder;

public class SearchPageActivity extends AppCompatActivity {

    public static final String SEARCH_TAG = "searchValue";
    private AppCompatActivity activity;
    private AppData appData;
    private Context app;
    private PlaylistFragment searchResultFragment;


    // Views
    private SearchView searchBar;
    private ProgressBar loadingIcon;
    private TextView searchTitle;
    //Media bar
    private MediaPlayerFragment mediaPlayerFragment;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);
        initVar();
        initMediaPlayer();
    }

    private void initVar() {
        activity = this;
        appData =((WikiAudioApp) getApplication()).getAppData();
        searchBar = findViewById(R.id.search_bar_view);
        searchBar.setIconified(false);
        searchBar.onActionViewExpanded();
        searchTitle = findViewById(R.id.search_title);
        searchTitle.setVisibility(View.GONE);
        app = getApplicationContext();
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Playlist searchResult = Holder.playlistsManager.createSearchBasedPlaylist(query);
                searchResultFragment = searchResult.getPlaylistFragment();
                searchResultFragment.showBorder(true);
                searchResultVisibility();
                searchTitle.setText(query);
                searchTitle.setVisibility(View.VISIBLE);
                searchBar.onActionViewCollapsed();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchResultVisibility() {
        if(searchResultFragment == null)
        {
            return;
        }
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.search_result_placeholder, searchResultFragment)
                .commit();
    }

    public void openSearchBar() {
        if(searchBar != null)
        {
            searchBar.onActionViewExpanded();
        }
    }

    private void initMediaPlayer() {
        mediaPlayerFragment = (MediaPlayerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mediaPlayerFragment);
        mediaPlayer = new MediaPlayer(activity, appData, mediaPlayerFragment);
        mediaPlayerFragment.setAudioPlayer(mediaPlayer);
        Holder.playlistsManager.setMediaPlayer(mediaPlayer);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null)
            mediaPlayer.pauseForActivityChange();
    }
}
