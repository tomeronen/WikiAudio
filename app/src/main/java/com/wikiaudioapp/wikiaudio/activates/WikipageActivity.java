package com.wikiaudioapp.wikiaudio.activates;

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
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wikiaudioapp.wikiaudio.R;
import com.wikiaudioapp.wikiaudio.WikiAudioApp;
import com.wikiaudioapp.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.wikiaudioapp.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.wikiaudioapp.wikiaudio.activates.playlist.Playlist;
import com.wikiaudioapp.wikiaudio.activates.playlist.PlaylistsManager;
import com.wikiaudioapp.wikiaudio.activates.record_page.WikiRecordActivity;
import com.wikiaudioapp.wikiaudio.data.AppData;
import com.wikiaudioapp.wikiaudio.data.Holder;
import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.Wikipage;

public class WikipageActivity extends AppCompatActivity {
    //For logs
    private static final String TAG = "WikipageActivity";

    //Vars
    private AppCompatActivity activity;
    private AppData appData;
    private String playlistTitle;
    private int wikipageIndexInPlaylist;
    private Wikipage wikipage;

    //MediaPlayer
    private MediaPlayerFragment mediaPlayerFragment;
    private MediaPlayer mediaPlayer;

    //Views
    private ImageView articleImage;
    private WebView webView;
    private FloatingActionButton recordButton;
    private FloatingActionButton playButton;
    private ImageButton addButton;
    private static boolean firstTimeOpening = false;
    private boolean showArticleImage;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_page);
        firstTimeOpening = (savedInstanceState == null);
        getIntentExtras();
        initVars();
        initMediaPlayer();
        setLayout();
        initOnClickButtons();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldDisplayPlayButton()) {
            playButton.setVisibility(View.VISIBLE);
        } else {
            playButton.setVisibility(View.GONE);
        }
    }

    /**
     * Pretty self-explanatory, really. Will finish the activity if bad intents were given.
     */
    private void getIntentExtras() {
        Intent intent = getIntent();
        playlistTitle = intent.getStringExtra("playlistTitle");
        wikipageIndexInPlaylist = intent.getIntExtra("index", -1);
        if (playlistTitle == null || wikipageIndexInPlaylist == -1) {
            Log.d(TAG, "onCreate: null extras from previous activity");
            finish();
        }
    }

    /**
     * Pretty self-explanatory, really.
     */
    private void initVars() {
        activity = this;
        appData =((WikiAudioApp) getApplication()).getAppData();
        //Views
        recordButton = findViewById(R.id.recordButton);
        playButton = findViewById(R.id.playButton);
        addButton = findViewById(R.id.addButton);
        webView = findViewById(R.id.WikipageView);
        articleImage = findViewById(R.id.thumbnailImageView);

        // Preferences:
        showArticleImage = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("articleImage", true);
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

    /**
     * Creates the click listeners for the record, play and add buttons.
     */
    private void initOnClickButtons() {
        recordButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, WikiRecordActivity.class);
            intent.putExtra(WikiRecordActivity.WIKI_PAGE_TAG, wikipage.getTitle());
            startActivity(intent);
        });

        playButton.setOnClickListener(v -> {
            // if the media player now holds this wikipage, play it if it's on pause
            if (mediaPlayer != null){
                if (mediaPlayer.getCurrentWikipage() != null &&
                        mediaPlayer.getCurrentWikipage().getTitle().equals(wikipage.getTitle())) {
                        if (!mediaPlayer.getIsPlaying()){
                            Log.d(TAG, "initOnClickButtons - PLAY: track was paused");
                            mediaPlayer.playCurrent();
                        }
                    return;
                }
                Log.d(TAG, "initOnClickButtons - PLAY: CurrentWikipage() == null");
                // create a playlist based on this wikipage solely and play it
                Playlist playlist = new Playlist(wikipage);
                PlaylistsManager.addPlaylist(playlist);
                mediaPlayer.play(playlist, 0);
           } else {
                Log.d(TAG, "initOnClickButtons - PLAY: null media player");
            }
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
     * Pretty self-explanatory, really.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void setLayout() {
        wikipage = Holder.playlistsManager
                .getWikipageByPlaylistTitleAndIndex(playlistTitle, wikipageIndexInPlaylist);
        if (wikipage == null || wikipage.getTitle() == null || wikipage.getUrl() == null) {
            Log.d(TAG, "setLayout: got null wikipage or title or URL");
            finish();
        }

        //WebView
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(wikipage.getUrl());

        //Image
        if(!showArticleImage || wikipage.getThumbnailSrc() == null) {
            articleImage.setVisibility(View.GONE);
        } else {
            if(firstTimeOpening) // don't show image again on rotation.
            {
                setFloatingButtonsVisibility(View.GONE);
                articleImage.bringToFront();
                displayImage();
            }
            else
            {
                articleImage.setVisibility(View.GONE);
                setFloatingButtonsVisibility(View.VISIBLE);
            }
        }
        shakeView(recordButton);
    }

    /**
     * Displays an animation of the wikipage's image on start
     */
    private void displayImage() {
        Glide.with(this)
                .asDrawable()
                .load(wikipage.getThumbnailSrc())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        articleImage.setVisibility(View.GONE);
                        setFloatingButtonsVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        fadeOut(articleImage);
                        return false;
                    }
                }).into(articleImage);
    }

    /**
     * Fade out animation.
     * @param view a view such as LinearLayout, RelativeLayout, etc.
     */
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
                setFloatingButtonsVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
            }
        });
        view.startAnimation(animFadeIn);
    }

    private void shakeView(View view) {

        if(view != null)
        {
            Animation animWobble = AnimationUtils.loadAnimation(getApplicationContext()
                    ,R.anim.wobble);
            animWobble.setStartOffset(10);
        animWobble.setAnimationListener(new Animation.AnimationListener(){
            int totalAmountOfShakes = 2;
            int curAmountOfShakes = 0;
            boolean pausedAnim = false;
            int repeatCount = 0;
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
                ++repeatCount;
                if(pausedAnim){ // we finished the pause.
                    animWobble.setStartOffset(0);
                    pausedAnim = false;
                }
                if(repeatCount % 20 == 0)
                {
                    ++curAmountOfShakes;
                    pausedAnim = true;
                    animWobble.setStartOffset(3000);
                }
                if(curAmountOfShakes > totalAmountOfShakes)
                {
                    view.clearAnimation();
                }
            }
            @Override
            public void onAnimationEnd(Animation arg0) {

            }
        });
            view.startAnimation(animWobble);
        }

    }

    /**
     * A simple solution to displaying the play button
     */
    private boolean shouldDisplayPlayButton() {
        //If the MediaPlayer plays this wikipage, hide the play button
        return !(mediaPlayer == null || mediaPlayer.getCurrentWikipage() == null ||
                mediaPlayer.getCurrentWikipage().getTitle().equals(wikipage.getTitle()));
    }

    /**
     * Pretty self-explanatory, really.
     */
    private void setFloatingButtonsVisibility(int visibility) {
        recordButton.setVisibility(visibility);
        addButton.setVisibility(visibility);
        //Display the play button only if it is not played && got View.VISIBLE
        if (shouldDisplayPlayButton() && visibility == View.VISIBLE) {
            playButton.setVisibility(View.VISIBLE);
        } else {
            playButton.setVisibility(View.GONE);
        }
    }

}