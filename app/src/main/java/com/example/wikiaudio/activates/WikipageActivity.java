package com.example.wikiaudio.activates;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.wikiaudio.data.AppData;
import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.R;
import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.activates.loading.LoadingActivity;
import com.example.wikiaudio.activates.loading.LoadingHelper;
import com.example.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.example.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.example.wikiaudio.activates.playlist.Playlist;
import com.example.wikiaudio.activates.playlist.PlaylistsManager;
import com.example.wikiaudio.activates.record_page.WikiRecordActivity;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.server.WorkerListener;
import com.example.wikiaudio.wikipedia.wikipage.PageAttributes;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class WikipageActivity extends AppCompatActivity {

    private static final String TAG = "WikipageActivity";

    private AppCompatActivity activity;
    private AppData appData;
    private LoadingHelper loadingHelper;

    private String wikipageTitle;
    private String playlistTitle;
    private int wikipageIndexInPlaylist;

    private Wikipage wikipage;
    private List<PageAttributes> pageAttributes;

    private MediaPlayerFragment mediaPlayerFragment;
    private MediaPlayer mediaPlayer;

    //Views
    private ImageView articleImage;
    private WebView webView;
    private FloatingActionButton recordButton;
    private FloatingActionButton playButton;
    private ImageButton addButton;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_page);
        getIntentExtras();
        initVars();
        initMediaPlayer();

        if (wikipageTitle != null) {
            setLayoutForTitleBased();
            setLoadingScreen();
        } else {
            setLayoutForWikipageBased();
        }

        initOnClickButtons();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null)
            mediaPlayer.pauseForActivityChange();
    }

    /**
     * Pretty self-explanatory, really. Will finish the activity if bad intents were given.
     */
    private void getIntentExtras() {
        Intent intent = getIntent();

        playlistTitle = intent.getStringExtra("playlistTitle");
        wikipageIndexInPlaylist = intent.getIntExtra("index", -1);
        wikipageTitle = intent.getStringExtra("title");

        if (!((wikipageTitle == null && playlistTitle != null && wikipageIndexInPlaylist > -1)
            || (wikipageTitle != null && playlistTitle == null)
            ||  (wikipageTitle != null && wikipageIndexInPlaylist < 0))) {
            Log.d(TAG, "onCreate: null extras in previous intent");
            finish();
        }
    }

    /**
     * Pretty self-explanatory, really.
     */
    private void initVars() {
        activity = this;
        appData =((WikiAudioApp) getApplication()).getAppData();
        loadingHelper = LoadingHelper.getInstance();

        webView = findViewById(R.id.WikipageView);
        articleImage = findViewById(R.id.thumbnailImageView);
        recordButton = findViewById(R.id.recordButton);
        playButton = findViewById(R.id.playButton);
        addButton = findViewById(R.id.addButton);
    }

    private void initMediaPlayer() {
        mediaPlayerFragment = (MediaPlayerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mediaPlayerFragment);
        mediaPlayer = new MediaPlayer(activity, appData, mediaPlayerFragment);
        mediaPlayerFragment.setAudioPlayer(mediaPlayer);
        Holder.playlistsManager.setMediaPlayer(mediaPlayer);
    }

    /**
     * If this activity was invoked with a title of a wikipage, we must get it from the wikiserver.
     */
    private void setLayoutForTitleBased() {
        pageAttributes = new ArrayList<>();
        pageAttributes.add(PageAttributes.title);
        pageAttributes.add(PageAttributes.thumbnail);
        pageAttributes.add(PageAttributes.content);
        pageAttributes.add(PageAttributes.url);
        wikipage = new Wikipage();
        fetchWikipage();
    }

    /**
     * If we already got the wikipage, then we only need to get its data by its playlist and index.
     */
    private void setLayoutForWikipageBased() {
        wikipage = Holder.playlistsManager
                .getWikipageByPlaylistTitleAndIndex(playlistTitle, wikipageIndexInPlaylist);
        if (wikipage == null) {
            Log.d(TAG, "initVars: got null wikipages from getWikipageByPlaylistTitleAndIndex");
            finish();
        }
        setLayout();
    }

    /**
     * Transfers to the loading screen activity while we fetch the wikipage object from the wiki server
     */
    private void setLoadingScreen(){
        int index = loadingHelper.loadWikipage(wikipage);
        if (index == -1) {
            Log.d(TAG, "setLoadingScreen: error loading wikipage to loading helper");
        } else {
            Intent loadingScreen = new Intent(activity, LoadingActivity.class);
            loadingScreen.putExtra("index", index);
            startActivity(loadingScreen);
        }
    }

    private void initOnClickButtons() {
        recordButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, WikiRecordActivity.class);
            Gson gson = new Gson();
            String wiki = gson.toJson(wikipage);
            intent.putExtra(WikiRecordActivity.WIKI_PAGE_TAG, wiki);
            startActivity(intent);
        });

        playButton.setOnClickListener(v -> {
            // if the user wants to only play this article, we create a playlist based on it solely
            // and play that playlist (the user might add wikipages to this playlist)
            Playlist playlist = new Playlist(wikipage);
            PlaylistsManager.addPlaylist(playlist);
            mediaPlayer.play(playlist, 0);
        });

        addButton.setOnClickListener(v -> {
            Playlist playlist = mediaPlayer.getCurrentPlaylist();
            if (playlist != null) {
                // we add the current wikipage to the end of the current playlist
                playlist.addWikipage(wikipage);
            } else {
                // it is just like clicking play
                playButton.performClick();
            }
        });


    }

    /**
     * Fetches the wikipage object from the wiki server
     */
    private void fetchWikipage() {
        Wikipedia wikipedia = Holder.wikipedia;
        if (wikipedia == null) {
            Log.d(TAG, "fetchWikipage, Handler.wikipedia == null");
            wikipedia = new Wikipedia(activity);

        }
        wikipedia.getWikipage(wikipageTitle, pageAttributes, wikipage, new WorkerListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess, fetchWikipage");
                setLayout();
            }
            @Override
            public void onFailure() {
                Log.d(TAG, "fetchWikipage-getWikipage-onFailure: couldn't get the Wikipage");
                finish();
            }
        });
    }

    /**
     * Pretty self-explanatory, really.
     */
//    Because WebView consumes web content that can include HTML and JavaScript, which may cause
//    security issues if you haven’t used it properly. Here, XSS stands for “cross-site scripting”
//    which is a form of hacking and by enabling client-side script into WebView which user is
//    accessing from application and this way you are opening up your application to such attacks.
//    I enabled it so the user can actually view the page and not be redirected to the google
//    chrome app. We should consider changing this.
    @SuppressLint("SetJavaScriptEnabled")
    private void setLayout() {
        if (wikipage.getTitle() == null || wikipage.getUrl() == null) {
            Log.d(TAG, "setLayout: got null title or url");
        }
        articleImage.bringToFront();

        //WebView
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(wikipage.getUrl());
        if(wikipage.getThumbnailSrc() == null)
        {
            articleImage.setVisibility(View.GONE);
            return;
        }

        // load Image
        Glide.with(this)
                .asDrawable()
                .load(wikipage.getThumbnailSrc())
                .listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                articleImage.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model,
                                           Target<Drawable> target,
                                           DataSource dataSource,
                                           boolean isFirstResource) {
                fadeOut(articleImage);
                articleImage.setOnClickListener(v -> {
                    articleImage.setVisibility(View.GONE);
                });
                return false;
            }
        }).into(articleImage);

    }

    private void fadeOut(final View view) {
        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
        animFadeIn.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                view.setVisibility(View.GONE);
            }
        });
        view.startAnimation(animFadeIn);
    }

}