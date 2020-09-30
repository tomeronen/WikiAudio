package com.example.wikiaudio.activates.record_page;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wikiaudio.R;

import java.util.List;

public class SectionsDialog extends DialogFragment {

    private SectionsAdapter sectionsAdapter;
    private List<String> sectionsName;
    private ViewGroup viewGroup;
    public AlertDialog alertDialog;
    public Window window1;

    public SectionsDialog(List<String> sectionsName) {
        this.sectionsName = sectionsName;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        View inflate = getLayoutInflater().inflate(R.layout.dialog_sections, null);
//        RecyclerView sectionsRecycler = inflate.findViewById(R.id.sectionsSumView);
//        sectionsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
//        sectionsAdapter = new SectionsAdapter(sectionsName);
//        sectionsRecycler.setAdapter(this.sectionsAdapter);
//        // Use the Builder class for convenient dialog construction
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setView(inflate);
//        // Create the AlertDialog object and return it
//        return builder.create();


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View inflate  = inflater.inflate(R.layout.dialog_sections, null);
        RecyclerView recyclerView = inflate.findViewById(R.id.sectionsSumView);
        recyclerView.setLayoutManager( new LinearLayoutManager(getContext()));
        sectionsAdapter = new SectionsAdapter(getActivity(), sectionsName);
        recyclerView.setAdapter(sectionsAdapter);


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflate);
        alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.gravity = Gravity.BOTTOM | Gravity.LEFT;
         return alertDialog;
    }

}
