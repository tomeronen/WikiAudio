package com.wikiaudioapp.wikiaudio.activates.record_page;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiaudioapp.wikiaudio.R;

import java.util.List;

public class SectionsDialog extends DialogFragment {

    private SectionsAdapter sectionsAdapter;
    private  List<SectionRecordingData> sectionsRecordingData;
    private ViewGroup viewGroup;
    public AlertDialog alertDialog;
    public Window window1;
    private MediaPlayer mp;

    public SectionsDialog( List<SectionRecordingData> sectionsName) {
        this.sectionsRecordingData = sectionsName;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View inflate  = inflater.inflate(R.layout.dialog_sections, null);
        RecyclerView recyclerView = inflate.findViewById(R.id.sectionsSumView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        mp = new MediaPlayer();
        sectionsAdapter = new SectionsAdapter(getActivity(), sectionsRecordingData, mp);
        recyclerView.setAdapter(sectionsAdapter);
        DividerItemDecoration dividerItemDecoration
                = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflate);
        builder.setNegativeButton("go back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        wlp.gravity = Gravity.BOTTOM | Gravity.START;
//        window.lay
        wlp.gravity = Gravity.CENTER;
        alertDialog.setTitle("Sections:");
        alertDialog.setIcon(R.drawable.sections_icon);
        return alertDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mp != null)
        {
            mp.release();
        }
    }
}
