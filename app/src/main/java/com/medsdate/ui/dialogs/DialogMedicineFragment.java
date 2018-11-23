package com.medsdate.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class DialogMedicineFragment extends DialogFragment {

    public static final String ARG_TITLE = "DialogErrorFragment.ARG_TITLE";
    public static final String ARG_MESSAGE = "DialogErrorFragment.ARG_MESSAGE";

    private boolean cancelable;
    private boolean changeTitle;

    private OnDialogMedicineListener listener;


    public static DialogMedicineFragment newInstance() {
        return newInstance(true, null);
    }

    public static DialogMedicineFragment newInstance(boolean cancelable, OnDialogMedicineListener listener) {
        DialogMedicineFragment fragment = new DialogMedicineFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        fragment.cancelable = cancelable;

        if(listener != null) {
            fragment.listener = listener;
        }

        return fragment;
    }

    public static DialogMedicineFragment newInstance(boolean cancelable, boolean changeTitle, OnDialogMedicineListener listener) {
        DialogMedicineFragment fragment = new DialogMedicineFragment();

        fragment.cancelable = cancelable;
        fragment.changeTitle = changeTitle;

        if(listener != null) {
            fragment.listener = listener;
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //title = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        this.setCancelable(cancelable);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        /*LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_welcome, null);

        TextView titleTextView = dialogView.findViewById(R.id.txt_welcome_title);
        if(changeTitle) {
            titleTextView.setText(R.string.txt_welcome_title_2);
        } else titleTextView.setText(R.string.txt_welcome_title);

        Button startTrial = dialogView.findViewById(R.id.btn_welcome_trial);
        startTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogMedicineFragment.this.getDialog().cancel();

                if(listener  != null) {
                    listener.startTrial();
                }
            }
        });

        TextView skipTrial = dialogView.findViewById(R.id.txt_welcome_no_trial);
        skipTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogMedicineFragment.this.getDialog().cancel();
            }
        });

        TextView accediTrial = dialogView.findViewById(R.id.txt_welcome_accedi);
        accediTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener  != null) {
                    listener.startAccedi();
                }
            }
        });

        // Create the AlertDialog object and return it
        builder.setView(dialogView);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(cancelable);*/

        return builder.create();
    }

    public interface OnDialogMedicineListener {
        void startTrial();
        void startAccedi();
    }

}
