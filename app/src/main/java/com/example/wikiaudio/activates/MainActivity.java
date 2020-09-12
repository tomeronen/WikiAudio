package com.example.wikiaudio.activates;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;

import com.example.wikiaudio.R;
import com.example.wikiaudio.location.LocationTracker;
import com.example.wikiaudio.wikipedia.Wikipedia;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Wikipedia wikipedia;
    LocationTracker locationTracker;
    AppCompatActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WorkManager.getInstance(this).cancelAllWork();  // todo debug
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent rec = new Intent(this, SearchPageActivity.class);
        startActivity(rec);

//        wikipedia = Wikipedia.getInstance();
//        activity = this;
//        showCategories();
//        final List<WikiPage> pagesNear = new ArrayList<>();
//        final List<WikiPage> searchResults = new ArrayList<>();
//        ArrayList<PageAttributes> pageAttributes = new ArrayList<>();
//        pageAttributes.add(PageAttributes.title);
//        pageAttributes.add(PageAttributes.coordinates);
//        pageAttributes.add(PageAttributes.content);
//
//        wikipedia.getPagesNearby(32.0623506,
//                                34.7747997,
//                                    10000,
//                                            pagesNear,
//                                            pageAttributes,
//                                            new WorkerListener() {
//                    @Override
//                    public void onSuccess() {
//                        if(pagesNear.size() == 10)
//                        {
//                            Log.d("s","s");
//                        }
//
//                    }
//
//                    @Override
//                    public void onFailure() {
//
//                    }
//                });


//        WikiPage wp = WikiPage.getPageForTesting();
//        Gson gson = new Gson();
//        String pageJsonString = gson.toJson(wp);
//        Intent rec = new Intent(this, WikiRecordActivity.class);
//        rec.putExtra(WikiRecordActivity.WIKI_PAGE_TAG, pageJsonString);
//        startActivity(rec);


//        WikiPage a = WikiTextParser.parseWikiHtml("https://en.wikipedia.org/wiki/Quark");
//        wikipedia.login("a","b");
        // for debug:

//        wikipedia = Wikipedia.getInstance();
//        wikipedia.getPagesNearby(this,32.443814,34.892546);
//        showCategories();
//        locationTracker = new LocationTracker(this);
    }

    private void showCategories() {
        UUID loadCategoriesId = wikipedia.loadSpokenPagesCategories(this);

    }
}
