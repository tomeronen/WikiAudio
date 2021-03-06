package com.wikiaudioapp.wikiaudio.activates.search_page;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wikiaudioapp.wikiaudio.R;
import com.wikiaudioapp.wikiaudio.WikiAudioApp;
import com.wikiaudioapp.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.wikiaudioapp.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.wikiaudioapp.wikiaudio.activates.playlist.Playlist;
import com.wikiaudioapp.wikiaudio.activates.playlist.playlist_ui.PlaylistFragment;
import com.wikiaudioapp.wikiaudio.data.AppData;
import com.wikiaudioapp.wikiaudio.data.Holder;
import com.wikiaudioapp.wikiaudio.wikipedia.server.WorkerListener;

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
        loadingIcon = findViewById(R.id.loadingSearchResults);
        loadingIcon.setVisibility(View.GONE);
        app = getApplicationContext();
        searchTitle.setOnClickListener(v -> {
            searchBar.onActionViewCollapsed();
            searchBar.onActionViewExpanded();
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadingIcon.setVisibility(View.VISIBLE);
                Playlist searchResult = Holder.playlistsManager.createSearchBasedPlaylist(query,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        loadingIcon.setVisibility(View.GONE);
                    }
                    @Override
                    public void onFailure() {
                        loadingIcon.setVisibility(View.GONE);
                        Toast.makeText(activity,
                                R.string.search_result_error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
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

    /**
     * Creates the media player + navigation bar at the bottom.
     */
    private void initMediaPlayer() {
        mediaPlayerFragment = (MediaPlayerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mediaPlayerFragment);
        if (mediaPlayerFragment == null) {
            mediaPlayerFragment = new MediaPlayerFragment();
            mediaPlayerFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(R.id.mediaPlayerFragment,
                    mediaPlayerFragment, "mediaPlayerFragment").commit();
        }
        mediaPlayer = new MediaPlayer(activity, appData, mediaPlayerFragment);
        mediaPlayerFragment.setAudioPlayer(mediaPlayer);
        Holder.playlistsManager.setMediaPlayer(mediaPlayer);
    }

}