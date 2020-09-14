package com.example.wikiaudio.activates;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.PageAttributes;
import com.example.wikiaudio.wikipedia.WikiPage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WorkerListener;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.List;

public class WikipageActivity extends AppCompatActivity {
    //For logs
    private static final String TAG = "WikipageActivity";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_page);

        String pageName = "Grammy Award for Best Contemporary Jazz Album";
        Wikipedia wikipedia = new Wikipedia(this);

        List<PageAttributes> pageAttributes = new ArrayList<>();
        pageAttributes.add(PageAttributes.title);
        pageAttributes.add(PageAttributes.thumbnail);
        pageAttributes.add(PageAttributes.url);

        final WikiPage result = new WikiPage();


        wikipedia.getWikiPage(pageName,
                pageAttributes,
                result, new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        String title = result.getTitle();
                        Double lat = result.getLat();
                        Double lon  = result.getLon();
                        Log.d("", "onSuccess, WikiPage??");
                    }

                    @Override
                    public void onFailure() {
                        // something went wrong :(
                    }
                });

        SystemClock.sleep(10000);


        if (result.getTitle() == null) {
            Log.d(TAG, "result title's is null");
        } else {
            WebView wv;
            TextView articleTitle;
            ImageView articleImage;

            Log.d(TAG, "result title's is " + result.getTitle());
            wv = (WebView) findViewById(R.id.webView);
            articleTitle = (TextView) findViewById(R.id.title);
            articleImage = (ImageView) findViewById(R.id.image); //TODO

            articleTitle.setText(result.getTitle());

            wv.setWebViewClient(new WebViewClient());
            wv.getSettings().setJavaScriptEnabled(true);
            wv.loadUrl(result.getUrl());

        }


    }
}
