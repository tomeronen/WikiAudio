package com.example.wikiaudio.activates.choose_categories;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.wikiaudio.R;
import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WorkerListener;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class ChooseCategoriesActivity extends AppCompatActivity {
    private Wikipedia wikipedia= new Wikipedia(this);
    private List<String> categories = new ArrayList<>();
    private RecyclerView categoriesView;
    private SearchView searchCategoriesView;
    private AppCompatActivity app;
    private Button saveButton;
    private final int COLUMN_AMOUNT = 4;
    private ArrayList<String> chosenCategories;
    CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_categories);
        app = this;
        categoriesView = findViewById(R.id.categoriesView);
        searchCategoriesView = findViewById(R.id.searchCategorysView);
        saveButton = findViewById(R.id.saveChoice);
        final CategoryClickListeners categoryClickListeners = new CategoryClickListeners() {
            @Override
            public void onClick(String string) {
                Toast.makeText(app, string, Toast.LENGTH_SHORT).show();
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
            }
        });

         wikipedia.loadSpokenPagesCategories(categories, new WorkerListener() {
             @Override
             public void onSuccess() {
                 Log.d("load status", "loaded categories");
                 categoryAdapter =
                         new CategoryAdapter(app, categories, categoryClickListeners);
                 categoriesView.setLayoutManager(new GridLayoutManager(app, COLUMN_AMOUNT));
                 RecyclerView.ItemDecoration itemDecoration = new
                         DividerItemDecoration(app, DividerItemDecoration.HORIZONTAL);
                 categoriesView.addItemDecoration(new SpacesItemDecoration(10));
//                 int resId = R.anim.grid_layout_animation_from_bottom;
//                 LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(app, resId);
//                 categoriesView.setLayoutAnimation(animation);
                 categoriesView.setAdapter(categoryAdapter);
             }

             @Override
             public void onFailure() {
                 Log.d("load status", "loading categories failed");

             }
         });
    }
}