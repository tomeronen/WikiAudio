package com.example.wikiaudio.activates.record_page;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class SimpleAlertDialogFragment extends DialogFragment {

    private final String msg;
    private final String positive;
    private final String negative;
    private final DialogInterface.OnClickListener positiveClick;
    private final DialogInterface.OnClickListener negativeClick;

    public SimpleAlertDialogFragment(String Msg,
                                     String positive,
                                     String negative,
                                     DialogInterface.OnClickListener positiveClick,
                                     DialogInterface.OnClickListener negativeClick) {
        msg = Msg;
        this.positive = positive;
        this.negative = negative;
        this.positiveClick = positiveClick;
        this.negativeClick = negativeClick;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg)
                .setPositiveButton(positive, positiveClick)
                .setNegativeButton(negative, negativeClick);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
