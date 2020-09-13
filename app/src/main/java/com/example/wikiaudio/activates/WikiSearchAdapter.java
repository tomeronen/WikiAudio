package com.example.wikiaudio.activates;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.wikipedia.WikiPage;

import java.util.List;

class WikiSearchAdapter extends RecyclerView.Adapter {
    private List<WikiPage> results;
    private ResultClickListeners resultClickListeners;

    public static class ResultViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        public TextView textView;
        private ResultClickListeners resultClickListeners;

        public ResultViewHolder(TextView v, final ResultClickListeners resultClickListeners) {
            super(v);
            textView = v;
            this.resultClickListeners = resultClickListeners;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resultClickListeners.onClick(textView.getText().toString());
                }
            });
        }

    }



    public WikiSearchAdapter(List<WikiPage> results, ResultClickListeners resultClickListeners) {
        this.results = results;
        this.resultClickListeners = resultClickListeners;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View inflate = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.search_result_iteam, parent, false);
        TextView textView = new TextView(parent.getContext());
        textView.setTextSize(24);
        ResultViewHolder resultViewHolder = new ResultViewHolder(textView, this.resultClickListeners);
        return resultViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ResultViewHolder) holder).textView.setText(results.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
}
