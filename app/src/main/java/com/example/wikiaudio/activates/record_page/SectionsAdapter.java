package com.example.wikiaudio.activates.record_page;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.R;

import java.util.List;

public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.SectionViewHolder> {
    private LayoutInflater _layoutInflater;
    private List<String> sectionsName;
    private FragmentActivity activity;
    private List<String> dataSet;

    public SectionsAdapter(FragmentActivity activity, List<String> sectionsName) {
        this.sectionsName = sectionsName;
        this._layoutInflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate an item view.
        View groupView =
                this._layoutInflater.inflate(R.layout.item_section,
                        parent, false);
        return new SectionViewHolder(groupView);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        holder.set(this.sectionsName.get(position));
    }

    @Override
    public int getItemCount() {
        return this.sectionsName.size();
    }


    public class SectionViewHolder extends RecyclerView.ViewHolder
    {
        private View itemView;
        private SectionsAdapter _adapter;
        private TextView _sectionTitleView;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this._sectionTitleView = itemView.findViewById(R.id.sectionItemTitle);
        }

        public void set(String s) {
            this._sectionTitleView.setText(s);
        }
    }

}
