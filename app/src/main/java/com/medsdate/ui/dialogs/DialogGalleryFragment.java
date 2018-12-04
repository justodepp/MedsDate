package com.medsdate.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.medsdate.R;
import com.medsdate.data.db.AppDatabase;
import com.medsdate.ui.main.GalleryImageAdapter;
import com.medsdate.utils.AppExecutors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DialogGalleryFragment extends DialogFragment implements View.OnClickListener {

    private boolean cancelable;

    private OnDialogGalleryListener listener;
    // Member variable for the Database
    private AppDatabase mDb;

    private View dialogView;
    private GridView gridView;
    private GalleryImageAdapter mAdapter;

    public static DialogGalleryFragment newInstance() {
        return newInstance(true, null);
    }

    public static DialogGalleryFragment newInstance(boolean cancelable, OnDialogGalleryListener listener) {
        DialogGalleryFragment fragment = new DialogGalleryFragment();
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
        dialogView = inflater.inflate(R.layout.dialog_gallery, null);
        mDb = AppDatabase.getInstance(getContext(), AppExecutors.getInstance());

        init();

        // Create the AlertDialog object and return it
        builder.setView(dialogView);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(cancelable);

        return builder.create();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.spinner_quantity) {
        }
    }

    private void init() {
        //String[] images = getActivity().getAssets().list("images");
        String[] images = new String[0];
        try {
            images = getActivity().getAssets().list("img_meds");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> listImages = new ArrayList<String>(Arrays.asList(images));

        gridView = dialogView.findViewById(R.id.grid_gallery);
        mAdapter = new GalleryImageAdapter(getContext(), listImages);

        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listener.setImageFromGallery(listImages.get(i));
                dismiss();
            }
        });
    }

    public interface OnDialogGalleryListener {
        void setImageFromGallery(String name);
    }
}
