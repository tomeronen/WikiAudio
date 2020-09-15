package com.example.wikiaudio.activates.choose_categories;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.WikiAudioApp;

import java.util.ArrayList;
import java.util.List;
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>{
    private CategoryClickListeners categoryClickListener;

    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View itemView;
        private final CategoryAdapter _adapter;
        private CategoryClickListeners categoryClickListener;
        private TextView _categoryNameView;

        public CategoryViewHolder(@NonNull View itemView, CategoryAdapter groupsAdapter,
                                  CategoryClickListeners categoryClickListener) {
            super(itemView);
            this._categoryNameView = itemView.findViewById(R.id.categoryName);
            this.itemView = itemView;
            this._adapter = groupsAdapter;
            this.categoryClickListener = categoryClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String name  = this._categoryNameView.getText().toString();
            if(!_categoriesChosen.contains(name))
            {
                _categoriesChosen.add(this._categoryNameView.getText().toString());
                itemView.setBackgroundColor(Color.parseColor("#7DCDF1"));
            }
            else
            {
                _categoriesChosen.remove(this._categoryNameView.getText().toString());
                itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            this.categoryClickListener.onClick(this._categoryNameView.getText().toString());
        }

        public void set(String categoryName){
            if(_categoriesChosen.contains(categoryName))
            {
                itemView.setBackgroundColor(Color.parseColor("#7DCDF1"));
            }
            else
            {
                itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            this._categoryNameView.setText(categoryName);
        }
    }


    private List<String> _categories;
    public List<String> _categoriesChosen;
    private LayoutInflater _layoutInflater;

    public CategoryAdapter(AppCompatActivity activity,
                           List<String> categoriesList,
                           CategoryClickListeners categoryClickListener){
        this._categories = categoriesList;
        this._layoutInflater = LayoutInflater.from(activity);
        this.categoryClickListener = categoryClickListener;
        this._categoriesChosen = ((WikiAudioApp)activity.getApplication())
                                            .getAppData().getChosenCategories();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate an item view.
        View groupView =
                this._layoutInflater.inflate(R.layout.category_item,
                        parent, false);
        return new CategoryViewHolder(groupView, this, this.categoryClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder groupHolder, int position) {
        groupHolder.set(this._categories.get(position));
    }

    @Override
    public int getItemCount() {
        return this._categories.size();
    }

    public void setOnClick()
    {
    }

}
