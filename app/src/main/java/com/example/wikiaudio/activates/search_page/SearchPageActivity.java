package com.example.wikiaudio.activates.search_page;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.PageAttributes;
import com.example.wikiaudio.wikipedia.WikiPage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WikipediaPlayer;
import com.example.wikiaudio.wikipedia.WorkerListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchPageActivity extends AppCompatActivity {

    EditText searchText;
    RecyclerView resultsView;
    FloatingActionButton searchButton;
    Wikipedia wikipedia;
    WikipediaPlayer wikipediaPlayer;
    Context app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);
        searchText = findViewById(R.id.searchText);
        resultsView = findViewById(R.id.searchResults);
        searchButton = findViewById(R.id.searchButton);
        wikipedia = new Wikipedia(this);
        wikipediaPlayer = new WikipediaPlayer(getApplicationContext(), Locale.ENGLISH, 0.8f);
        app = getApplicationContext();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToSearch = searchText.getText().toString();
                final List<WikiPage> results = new ArrayList<>();
                wikipedia.searchForPage(textToSearch, null, results,
                        new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        WikiSearchAdapter wikiSearchAdapter = new WikiSearchAdapter(results,
                                new ResultClickListeners() {
                                    @Override
                                    public void onClick(String string) {
                                        final WikiPage wikiPage = new WikiPage();
                                        List<PageAttributes> pageAttributes = new ArrayList<>();
                                        pageAttributes.add(PageAttributes.audioUrl);
                                        pageAttributes.add(PageAttributes.content);
                                        pageAttributes.add(PageAttributes.title);
                                        wikipedia.getWikiPage(string, pageAttributes, wikiPage,
                                                new WorkerListener() {
                                            @Override
                                            public void onSuccess() {
                                                WikipediaPlayer wikipediaPlayer
                                                        = new WikipediaPlayer(app, Locale.ENGLISH, 0.8f);
                                                wikipediaPlayer.playWiki(wikiPage);
                                            }

                                            @Override
                                            public void onFailure() {

                                            }
                                        });
                                    }
                                });
                        resultsView.setLayoutManager(new LinearLayoutManager(app));
                        resultsView.setAdapter(wikiSearchAdapter);
                    }
                    @Override
                    public void onFailure() {
                        Toast.makeText(SearchPageActivity.this,
                                "something went wrong with the search  :)",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
