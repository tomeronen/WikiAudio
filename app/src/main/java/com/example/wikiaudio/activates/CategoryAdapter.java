package com.example.wikiaudio.activates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.R;

import java.util.List;
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>{
    private CategoryClickListeners categoryClickListener;

    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final CategoryAdapter _adapter;
        private CategoryClickListeners categoryClickListener;
        private TextView _categoryNameView;

        public CategoryViewHolder(@NonNull View itemView, CategoryAdapter groupsAdapter,
                                  CategoryClickListeners categoryClickListener) {
            super(itemView);
            this._categoryNameView = itemView.findViewById(R.id.categoryName);
            this._adapter = groupsAdapter;
            this.categoryClickListener = categoryClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            this.categoryClickListener.onClick(this._categoryNameView.getText().toString());
        }

        public void set(String categoryName){
            this._categoryNameView.setText(categoryName);
        }
    }


    private List<String> _categories;
    private final Context _context;
    private LayoutInflater _layoutInflater;

    public CategoryAdapter(Context context, List<String> categoriesList, CategoryClickListeners categoryClickListener){
        this._context = context;
        this._categories = categoriesList;
        this._layoutInflater = LayoutInflater.from(context);
        this.categoryClickListener = categoryClickListener;
    }

    public void addGroups(List<String> newCategories)
    {
        this._categories.addAll(newCategories);
        notifyDataSetChanged();
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
