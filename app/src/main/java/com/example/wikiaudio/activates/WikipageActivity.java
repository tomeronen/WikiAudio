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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.wikiaudio.R;
import com.example.wikiaudio.activates.loading.LoadingActivity;
import com.example.wikiaudio.activates.loading.LoadingHelper;
import com.example.wikiaudio.activates.record_page.WikiRecordActivity;
import com.example.wikiaudio.wikipedia.PageAttributes;
import com.example.wikiaudio.wikipedia.Wikipage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WorkerListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class WikipageActivity extends AppCompatActivity {

    private static final String TAG = "WikipageActivity";
    private TextView articleTitle;
    private ImageView articleImage;
    private WebView webView;
    private FloatingActionButton recordButton;

    private AppCompatActivity activity;
    private LoadingHelper loadingHelper;

    private String title;
    private Wikipedia wikipedia;
    private List<PageAttributes> pageAttributes;
    private Wikipage wikipage;
    private MediaPlayerFragment mediaPlayerFragment;
    private boolean firstPress = true;
    private boolean playing = false;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_page);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        if (title == null) {
            Log.d(TAG, "onCreate: null title from intent extra");
            finish();
        }

        initVars();
        fetchWikipage();
//        initMediaPlayer();
        initOnClickButtons();
        setLoadingScreen();


    }

//    private void initMediaPlayer() {
//        mediaPlayerFragment = (MediaPlayerFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.media_player);
//        mediaPlayerFragment.showTitle(false);
//
//        mediaPlayerFragment.playButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(firstPress &&
//                        wikipage.getSections() != null) {
//                    firstPress = false;
//                    playing = true;
//                    mediaPlayerFragment.addWikiToPlayList(wikipage, true);
//                }
//                if(!firstPress && !playing)
//                {
//                    mediaPlayerFragment.startPlaying();
//                }
//                if(playing)
//                {
//                    mediaPlayerFragment.pausePlaying();
//                }
//            }
//        });
//    }


    private void initOnClickButtons() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, WikiRecordActivity.class);
                Gson gson = new Gson();
                String wiki = gson.toJson(wikipage);
                intent.putExtra(WikiRecordActivity.WIKI_PAGE_TAG, wiki);
                startActivity(intent);
            }
        });


    }


    /**
     * Pretty self-explanatory, really.
     */
    private void initVars() {
        activity = this;
        loadingHelper = LoadingHelper.getInstance();

//        articleTitle = findViewById(R.id.title);
        webView = findViewById(R.id.WikipageView);
        articleImage = findViewById(R.id.thumbnailImageView);
        recordButton = findViewById(R.id.recordButton);

        wikipage = new Wikipage();
        wikipedia = new Wikipedia(this);
        pageAttributes = new ArrayList<>();
        pageAttributes.add(PageAttributes.title);
        pageAttributes.add(PageAttributes.thumbnail);
        pageAttributes.add(PageAttributes.content);
        pageAttributes.add(PageAttributes.url);
    }

    /**
     * Fetches the wikipage object from the wiki server
     */
    private void fetchWikipage() {
        wikipedia.getWikipage(title, pageAttributes, wikipage, new WorkerListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess, getWikipage");
                setLayout();
            }
            @Override
            public void onFailure() {
                Log.d(TAG, "fetchWikipage-getWikipage-onFailure: couldn't get the Wikipage");
            }
        });
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
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                fadeOut(articleImage);
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