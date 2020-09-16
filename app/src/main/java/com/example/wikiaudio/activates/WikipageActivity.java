package com.example.wikiaudio.activates;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.R;
import com.example.wikiaudio.activates.loading.LoadingActivity;
import com.example.wikiaudio.activates.loading.LoadingHelper;
import com.example.wikiaudio.wikipedia.PageAttributes;
import com.example.wikiaudio.wikipedia.Wikipage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WorkerListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class WikipageActivity extends AppCompatActivity {
    //For logs
    private static final String TAG = "WikipageActivity";

    private AppCompatActivity activity;
    private LoadingHelper loadingHelper;

    private String title;
    private Wikipedia wikipedia;
    private List<PageAttributes> pageAttributes;
    private Wikipage wikipage;

    private FloatingActionButton recordButton;
    private TextView background;
    private WebView webView;
    private TextView articleTitle;
    private ImageView articleImage;

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
        setLoadingScreen();
    }

    private void initVars() {
        activity = this;
        loadingHelper = LoadingHelper.getInstance();
        articleTitle = (TextView) findViewById(R.id.title);
        background = (TextView) findViewById(R.id.webBackground);
        webView = (WebView) findViewById(R.id.webView);
        articleImage = (ImageView) findViewById(R.id.image);
        recordButton = (FloatingActionButton) findViewById(R.id.floatingRecordButton);
        wikipedia = new Wikipedia(this);
        pageAttributes = new ArrayList<>();
        pageAttributes.add(PageAttributes.title);
        pageAttributes.add(PageAttributes.thumbnail);
        pageAttributes.add(PageAttributes.url);
        wikipage = new Wikipage();
    }

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

//    Because WebView consumes web content that can include HTML and JavaScript, which may cause
//    security issues if you haven’t used it properly. Here, XSS stands for “cross-site scripting”
//    which is a form of hacking and by enabling client-side script into WebView which user is
//    accessing from application and this way you are opening up your application to such attacks.
    //I enabled it so the user can actually view the page and not be redirected to the google
    // chrome app. We should consider changing this.
    @SuppressLint("SetJavaScriptEnabled")
    private void setLayout() {
        if (wikipage.getTitle() == null || wikipage.getUrl() == null) {
            Log.d(TAG, "setLayout: got null title or url");
        }

        //Title
        articleTitle.setText(wikipage.getTitle());

        //Image
        //todo

        //WebView
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(wikipage.getUrl());
    }
}
