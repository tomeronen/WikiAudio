package com.example.wikiaudio.activates;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.PageAttributes;
import com.example.wikiaudio.wikipedia.WikiPage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WorkerListener;

import java.util.ArrayList;
import java.util.List;

public class WikiPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_page);

        String pageName = "Super_Mario_All-Stars";
        Wikipedia wikipedia = new Wikipedia(this);

        List<PageAttributes> pageAttributes = new ArrayList<>();
        pageAttributes.add(PageAttributes.title);
        pageAttributes.add(PageAttributes.coordinates);

        final WikiPage result = new WikiPage();

        wikipedia.getWikiPage(pageName,
                pageAttributes,
                result, new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        String title = result.getTitle();
                        Double lat = result.getLat();
                        Double lon  = result.getLon();
                    }

                    @Override
                    public void onFailure() {
                        // something went wrong :(
                    }
                });
    }
}
