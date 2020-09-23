package com.example.wikiaudio.activates.choose_categories;

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
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WorkerListener;

import java.util.ArrayList;
import java.util.List;

public class ChooseCategoriesActivity extends AppCompatActivity {
    private Wikipedia wikipedia;
    private List<String> categories = new ArrayList<>();
    private RecyclerView categoriesView;
    private SearchView searchCategoriesView;
    private AppCompatActivity app;
    private Button saveButton;
    private int columnAmount;
    private ArrayList<String> chosenCategories;
    CategoryAdapter categoryAdapter;
    ProgressBar loadingIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_categories);
        app = this;
        categoriesView = findViewById(R.id.categoriesView);
        searchCategoriesView = findViewById(R.id.searchCategorysView);
        saveButton = findViewById(R.id.saveChoice);
        loadingIcon = findViewById(R.id.progressBar5);
        wikipedia = new Wikipedia(this);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            columnAmount = 6;
        } else {
            // In portrait
            columnAmount = 3;
        }

        final CategoryClickListeners categoryClickListeners = new CategoryClickListeners() {
            @Override
            public void onClick(String string) {
                // what to do when a category is pressed.
            }
        };
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categoryAdapter != null)
                {
                    ((WikiAudioApp) getApplication())
                            .getAppData()
                            .saveChosenCategories(categoryAdapter._categoriesChosen);
                }
                finish();
            }
        });

        loadingIcon.setVisibility(View.VISIBLE);
         wikipedia.loadSpokenPagesCategories(categories, new WorkerListener() {
             @Override
             public void onSuccess() {
                 Log.d("load status", "loaded categories");
                 categoryAdapter =
                         new CategoryAdapter(app, categories, categoryClickListeners);
                 categoriesView.setLayoutManager(new GridLayoutManager(app, columnAmount));
                 RecyclerView.ItemDecoration itemDecoration = new
                         DividerItemDecoration(app, DividerItemDecoration.HORIZONTAL);
                 categoriesView.addItemDecoration(new SpacesItemDecoration(10));
//                 int resId = R.anim.grid_layout_animation_from_bottom;
//                 LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(app, resId);
//                 categoriesView.setLayoutAnimation(animation);
                 categoriesView.setAdapter(categoryAdapter);
                 loadingIcon.setVisibility(View.GONE);
             }

             @Override
             public void onFailure() {
                 Log.d("load status", "loading categories failed");
                 loadingIcon.setVisibility(View.GONE);

             }
         });
    }
}