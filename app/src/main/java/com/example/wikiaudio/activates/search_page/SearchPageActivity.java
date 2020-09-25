package com.example.wikiaudio.activates.search_page;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.data.AppData;
import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.R;
import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.activates.WikipageActivity;
import com.example.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.example.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.example.wikiaudio.wikipedia.server.WorkerListener;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

import java.util.ArrayList;
import java.util.List;

public class SearchPageActivity extends AppCompatActivity {

    public static final String SEARCH_TAG = "searchValue";
    private AppCompatActivity activity;
    private AppData appData;
    private Context app;

    private RecyclerView resultsView;

    // Views
    private SearchView searchView;
    private ProgressBar loadingIcon;

    //Media bar
    private MediaPlayerFragment mediaPlayerFragment;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        appData =((WikiAudioApp) getApplication()).getAppData();
        setContentView(R.layout.activity_search_page);
        resultsView = findViewById(R.id.searchResults);
        searchView = findViewById(R.id.search_bar_view);
        loadingIcon = findViewById(R.id.progressBar2);
        app = getApplicationContext();
        String valueToSearch = getIntent().getStringExtra(SEARCH_TAG);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchValue(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setQuery(valueToSearch, true);
        searchView.setQueryHint(valueToSearch);
        initMediaPlayer();
    }

    private void searchValue(String textToSearch) {
        final List<Wikipage> results = new ArrayList<>();
        loadingIcon.setVisibility(View.VISIBLE);
        Holder.wikipedia.searchForPage(textToSearch, null, results,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        WikiSearchAdapter wikiSearchAdapter = new WikiSearchAdapter(results,
                                new com.example.wikiaudio.activates.search_page.ResultClickListeners() {
                                    @Override
                                    public void onClick(String string) {
//                                        final Wikipage Wikipage = new Wikipage();
//                                        List<PageAttributes> pageAttributes = new ArrayList<>();
//                                        pageAttributes.add(PageAttributes.audioUrl);
//                                        pageAttributes.add(PageAttributes.content);
//                                        pageAttributes.add(PageAttributes.title);
//                                        wikipedia.getWikipage(string, pageAttributes, Wikipage,
//                                                new WorkerListener() {
//                                                    @Override
//                                                    public void onSuccess() {
//                                                        WikipediaPlayer wikipediaPlayer
//                                                                = new WikipediaPlayer(app, Locale.ENGLISH, 0.8f);
//                                                        wikipediaPlayer.playWiki(Wikipage);
//                                                    }
//
//                                                    @Override
//                                                    public void onFailure() {
//
//                                                    }
//                                                });
                                        String title = string;
                                        Intent WikipageIntent = new Intent(app, WikipageActivity.class);
                                        WikipageIntent.putExtra("title", title);
                                        startActivity(WikipageIntent);
                                    }
                                });
                        resultsView.setLayoutManager(new LinearLayoutManager(app));
                        resultsView.setAdapter(wikiSearchAdapter);
                        loadingIcon.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onFailure() {
                        Toast.makeText(SearchPageActivity.this,
                                "something went wrong with the search  :)",
                                Toast.LENGTH_SHORT).show();
                        loadingIcon.setVisibility(View.GONE);
                    }
                });
    }

    // ADD CLICK LISTENER HERE WITH TODO

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
