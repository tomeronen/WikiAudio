package com.example.wikiaudio.activates.record_page;

import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.SectionViewHolder> {
    private MediaPlayer mediaPlayer;
    private final FragmentActivity activ;
    private LayoutInflater _layoutInflater;
    private List<SectionRecordingData> sectionsRecordingData;
    private FragmentActivity activity;
    private List<String> dataSet;


    public SectionsAdapter(FragmentActivity activity, List<SectionRecordingData> sectionsName,
                           MediaPlayer mediaPlayer) {
        this.sectionsRecordingData = sectionsName;
        activ = activity;
        this._layoutInflater = LayoutInflater.from(activity);
        this.mediaPlayer = mediaPlayer;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate an item view.
        View groupView =
                this._layoutInflater.inflate(R.layout.item_section,
                        parent, false);

        SectionViewHolder sectionViewHolder = new SectionViewHolder(groupView, this);
        sectionViewHolder._playSectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (sectionViewHolder.data != null
                            && sectionViewHolder.data.fileRecording != null
                            && sectionViewHolder.data.fileRecording.exists()) {
                        if(mediaPlayer.isPlaying())
                        {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = new MediaPlayer();
                        }
                        else
                        {
                            mediaPlayer.setDataSource(sectionViewHolder.data.fileRecording.getPath());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            sectionViewHolder.currentlyPlaying = true;
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        sectionViewHolder._deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PrintWriter writer = null;
                        writer = new
                                PrintWriter(sectionViewHolder.data.fileRecording.getPath());
                    writer.print("");
                    // other operations
                    writer.close();
                    sectionViewHolder.sectionsAdapter.notifyDataSetChanged();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        return sectionViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        holder.set(this.sectionsRecordingData.get(position));
    }

    @Override
    public int getItemCount() {
        return this.sectionsRecordingData.size();
    }


    public class SectionViewHolder extends RecyclerView.ViewHolder
    {
        public boolean currentlyPlaying;
        private View itemView;
        private TextView _sectionTitleView;
        private TextView _sectionTimer;
        private FloatingActionButton _playSectionButton;
        private FloatingActionButton _deleteButton;
        private SectionsAdapter sectionsAdapter;
        private SectionRecordingData data;

        public SectionViewHolder(@NonNull View itemView, SectionsAdapter sectionsAdapter) {
            super(itemView);
            this.itemView = itemView;
            this._sectionTitleView = itemView.findViewById(R.id.sectionItemTitle);
            this._sectionTimer = itemView.findViewById(R.id.sectionTime);
            this._playSectionButton = itemView.findViewById(R.id.playPart);
            this._deleteButton = itemView.findViewById(R.id.deletePart);
            this.sectionsAdapter = sectionsAdapter;
        }

        public void set(SectionRecordingData s) {
            this.data = s;
            this._sectionTitleView.setText(s.sectionTitle);
            if(s.fileRecording != null
                    && s.fileRecording.exists()
                    && s.fileRecording.length() > 0)
            { // there is a recording.
                if(s.milSeconds != 0)
                {
                    this._sectionTimer.setVisibility((View.VISIBLE));
                    this._sectionTimer.setText(timerStringFromMilsec(s.milSeconds));
                }
                this._playSectionButton.setVisibility((View.VISIBLE));
                this._deleteButton.setVisibility((View.VISIBLE));
                this._sectionTitleView.setTextColor(itemView.getResources().getColor(R.color.green));
            }
            else
            { // there is no recording.
                this._sectionTimer.setVisibility((View.GONE));
                this._playSectionButton.setVisibility((View.GONE));
                this._deleteButton.setVisibility((View.GONE));
                this._sectionTitleView.setTextColor(itemView.getResources().getColor(R.color.gray));
            }
        }

        private String timerStringFromMilsec(long milSeconds) {
            long millis = milSeconds;
            int seconds = (int) (millis / 60);
            int minutes = seconds / 60;
            seconds     = seconds % 60;
            return  String.format("%d:%02d:%02d", minutes, seconds,millis);
        }
    }

}
