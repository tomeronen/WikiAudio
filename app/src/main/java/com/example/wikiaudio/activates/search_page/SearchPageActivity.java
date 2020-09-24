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

import com.example.wikiaudio.R;
import com.example.wikiaudio.activates.WikipageActivity;
import com.example.wikiaudio.wikipedia.Wikipage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WikipediaPlayer;
import com.example.wikiaudio.wikipedia.WorkerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchPageActivity extends AppCompatActivity {

    public static final String SEARCH_TAG = "searchValue";
    RecyclerView resultsView;
    Wikipedia wikipedia;
    WikipediaPlayer wikipediaPlayer;
    Context app;
    SearchView searchView;
    ProgressBar loadingIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);
        resultsView = findViewById(R.id.searchResults);
        searchView = findViewById(R.id.search_bar_view);
        loadingIcon = findViewById(R.id.progressBar2);
        wikipedia = new Wikipedia(this);
        wikipediaPlayer = new WikipediaPlayer(this, Locale.ENGLISH, 0.8f);
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
    }

    private void searchValue(String textToSearch) {
        final List<Wikipage> results = new ArrayList<>();
        loadingIcon.setVisibility(View.VISIBLE);
        wikipedia.searchForPage(textToSearch, null, results,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        WikiSearchAdapter wikiSearchAdapter = new WikiSearchAdapter(results,
                                new com.example.wikiaudio.activates.search_page.ResultClickListeners() {
                                    @Override
                                    public void onClick(String string) {
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

}
