package com.example.wikiaudio.activates.choose_categories;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.R;
import com.example.wikiaudio.WikiAudioApp;

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
//                itemView.setBackgroundColor(Color.parseColor("#7DCDF1"));
                // highlight pick:
                this._categoryNameView.setBackgroundResource(R.drawable.rounded_corner_highlight);
                this._categoryNameView
                        .setTextColor(itemView.getResources().getColor(R.color.chosenItem));

            }
            else
            {
                _categoriesChosen.remove(this._categoryNameView.getText().toString());
//                itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                // unhighlight pick:
                this._categoryNameView.setBackgroundResource(R.drawable.rounded_corner);
                this._categoryNameView
                        .setTextColor(itemView.getResources().getColor(R.color.black));
            }
            // todo do we want to save chosen categories each time?
            ((WikiAudioApp) this._adapter.activity.getApplication())
                    .getAppData().saveChosenCategories(_categoriesChosen);

            this.categoryClickListener.onClick(this._categoryNameView.getText().toString());
        }

        public void set(String categoryName){
            this._categoryNameView.setText(categoryName);
            if(_categoriesChosen != null && _categoriesChosen.contains(categoryName))
            {
                this._categoryNameView.setBackgroundResource(R.drawable.rounded_corner_highlight);
                this._categoryNameView
                        .setTextColor(itemView.getResources().getColor(R.color.chosenItem));
            }
            else
            {
                this._categoryNameView.setBackgroundResource(R.drawable.rounded_corner);
                this._categoryNameView
                        .setTextColor(itemView.getResources().getColor(R.color.black));
            }
        }
    }


    private List<String> _categories;
    public List<String> _categoriesChosen;
    private LayoutInflater _layoutInflater;
    private final AppCompatActivity activity;

    public CategoryAdapter(AppCompatActivity activity,
                           List<String> categoriesList,
                           CategoryClickListeners categoryClickListener){
        this._categories = categoriesList;
        this.activity  = activity;
        this._layoutInflater = LayoutInflater.from(activity);
        this.categoryClickListener = categoryClickListener;
        this._categoriesChosen = ((WikiAudioApp)activity.getApplication())
                                            .getAppData().getChosenCategories();
        Log.d("test","test");
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
