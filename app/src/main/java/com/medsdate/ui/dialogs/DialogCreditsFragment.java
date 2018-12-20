package com.medsdate.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.medsdate.R;

public class DialogCreditsFragment extends DialogFragment {

    private boolean cancelable;

    private OnDialogCreditsListener listener;

    private View dialogView;

    public static DialogCreditsFragment newInstance() {
        return newInstance(true, null);
    }

    public static DialogCreditsFragment newInstance(boolean cancelable, OnDialogCreditsListener listener) {
        DialogCreditsFragment fragment = new DialogCreditsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        fragment.cancelable = cancelable;

        if(listener != null) {
            fragment.listener = listener;
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        this.setCancelable(cancelable);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_credits, null);

        // Create the AlertDialog object and return it
        builder.setView(dialogView);
        Dialog dialog = builder.create();

        TextView title = dialogView.findViewById(R.id.title_credits_text);
        title.setText(R.string.title_credits_text);
        TextView message = dialogView.findViewById(R.id.msg_credits_text);
        message.setText(getString(R.string.msg_credits_text));

        dialog.setCanceledOnTouchOutside(cancelable);

        return builder.create();
    }

    public interface OnDialogCreditsListener {
    }
}
