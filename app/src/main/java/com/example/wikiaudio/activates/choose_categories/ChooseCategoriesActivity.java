package com.example.wikiaudio.activates.choose_categories;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.example.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.example.wikiaudio.data.AppData;
import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.wikipedia.server.WorkerListener;

import java.util.ArrayList;
import java.util.List;

public class ChooseCategoriesActivity extends AppCompatActivity {

    private static final String TAG = "ChooseCategoriesActivity";

    private AppCompatActivity activity;
    private AppData appData;

    private List<String> originalCategories = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private int columnAmount;
    private CategoryAdapter categoryAdapter;

    //Views
    private SearchView searchCategoriesView;
    private RecyclerView categoriesView;
    private Button saveButton;
    private ProgressBar loadingIcon;

    //Media bar
    private MediaPlayerFragment mediaPlayerFragment;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_categories);
        initVars();
        initOnClickButtons();
        setOrientation();
        loadCategories();
        initMediaPlayer();
    }

    private void setOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            columnAmount = 6;
        } else {
            // In portrait
            columnAmount = 3;
        }
    }

    private void initVars() {
        activity = this;
        appData =((WikiAudioApp) getApplication()).getAppData();
        this.originalCategories = ((WikiAudioApp)activity.getApplication())
                .getAppData().getChosenCategories();
        categoriesView = findViewById(R.id.categoriesView);
        searchCategoriesView = findViewById(R.id.searchCategorysView);
        saveButton = findViewById(R.id.saveChoice);
        loadingIcon = findViewById(R.id.progressBar5);
    }

    private void initOnClickButtons() {
        saveButton.setOnClickListener(v -> {
            if (!(originalCategories.containsAll(categories) &&
                    categories.containsAll(originalCategories))) {
                // categories changed
                ((WikiAudioApp) getApplication()).getAppData()
                        .saveChosenCategories(categoryAdapter._categoriesChosen);
                Intent intent = getIntent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            // categories unchanged
            Intent intent = getIntent();
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
        });
    }

    private void loadCategories() {
        final CategoryClickListeners categoryClickListeners = string -> {};
        loadingIcon.setVisibility(View.VISIBLE);
        Holder.wikipedia.loadSpokenPagesCategories(categories, new WorkerListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "load status: loaded categories");
                categoryAdapter =
                        new CategoryAdapter(activity, categories, categoryClickListeners);
                categoriesView.setLayoutManager(new GridLayoutManager(activity, columnAmount));
                RecyclerView.ItemDecoration itemDecoration = new
                        DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL);
                categoriesView.addItemDecoration(new SpacesItemDecoration(10));
                categoriesView.setAdapter(categoryAdapter);
                loadingIcon.setVisibility(View.GONE);
            }

            @Override
            public void onFailure() {
                Log.d(TAG, "load status: loading categories failed");
                loadingIcon.setVisibility(View.GONE);
            }
        });
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