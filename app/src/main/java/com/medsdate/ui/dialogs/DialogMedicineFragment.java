package com.medsdate.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.medsdate.R;
import com.medsdate.data.db.AppDatabase;
import com.medsdate.utils.AppExecutors;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DialogMedicineFragment extends DialogFragment implements View.OnClickListener {

    public static final String EXTRA_TASK_ID = "extraTaskId";
    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_TASK_ID = -1;

    private boolean cancelable;
    private boolean isNewMedicine;

    private OnDialogMedicineListener listener;

    private int mTaskId = DEFAULT_TASK_ID;
    // Member variable for the Database
    private AppDatabase mDb;

    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener date;

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

    public static DialogMedicineFragment newInstance(boolean cancelable, boolean isNewMedicine, OnDialogMedicineListener listener) {
        DialogMedicineFragment fragment = new DialogMedicineFragment();

        fragment.cancelable = cancelable;
        fragment.isNewMedicine = isNewMedicine;

        if(listener != null) {
            fragment.listener = listener;
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTaskId = getArguments().getInt(EXTRA_TASK_ID);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        this.setCancelable(cancelable);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_medicine, null);

        mDb = AppDatabase.getInstance(getContext(), AppExecutors.getInstance());



        if(isNewMedicine) {

        } else {

        }

/*        startTrial.setOnClickListener(new View.OnClickListener() {
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

                if (intent != null && intent.hasExtra(EXTRA_TASK_ID)) {
            mButton.setText(R.string.update_button);
            if (mTaskId == DEFAULT_TASK_ID) {
                // populate the UI
                mTaskId = intent.getIntExtra(EXTRA_TASK_ID, DEFAULT_TASK_ID);

                AddTaskViewModelFactory factory = new AddTaskViewModelFactory(mDb, mTaskId);
                final AddTaskViewModel viewModel
                        = ViewModelProviders.of(this, factory).get(AddTaskViewModel.class);

                // COMPLETED (12) Observe the LiveData object in the ViewModel. Use it also when removing the observer
                viewModel.getTask().observe(this, new Observer<TaskEntry>() {
                    @Override
                    public void onChanged(@Nullable TaskEntry taskEntry) {
                        viewModel.getTask().removeObserver(this);
                        populateUI(taskEntry);
                    }
                });
            }
        }
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
        });*/

        // Create the AlertDialog object and return it
        builder.setView(dialogView);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(cancelable);

        dateChooser();
        return builder.create();
    }

    @Override
    public void onClick(View view) {
        new DatePickerDialog(getContext(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public interface OnDialogMedicineListener {
        void saveMedicine();
        void updateMedicine();
    }

    private void dateChooser() {
        myCalendar = Calendar.getInstance();

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        //String myFormat = "yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        //annoReg.setText(sdf.format(myCalendar.getTime()));
    }
}
