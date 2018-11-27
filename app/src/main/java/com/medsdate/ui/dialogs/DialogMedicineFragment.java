package com.medsdate.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.medsdate.R;
import com.medsdate.data.db.AppDatabase;
import com.medsdate.data.db.model.MedicineEntry;
import com.medsdate.ui.viewmodel.MedsViewModel;
import com.medsdate.utils.AppExecutors;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DialogMedicineFragment extends DialogFragment implements View.OnClickListener {

    public static final String EXTRA_TASK_ID = "extraTaskId";
    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_TASK_ID = -1;

    private boolean cancelable;

    private OnDialogMedicineListener listener;

    private int mMedicineId = DEFAULT_TASK_ID;
    // Member variable for the Database
    private AppDatabase mDb;

    private View dialogView;

    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener date;

    private TextView mDay;
    private TextView mMonth;
    private TextView mYear;

    private EditText mName;
    private TextView mQuantity;
    private int currentQuantity = 1;

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

    public static DialogMedicineFragment newInstance(boolean cancelable, int extra_id, OnDialogMedicineListener listener) {
        DialogMedicineFragment fragment = new DialogMedicineFragment();

        fragment.cancelable = cancelable;
        fragment.mMedicineId = extra_id;

        if(listener != null) {
            fragment.listener = listener;
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMedicineId = getArguments().getInt(EXTRA_TASK_ID, DEFAULT_TASK_ID);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        this.setCancelable(cancelable);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_medicine, null);

        dialogView.findViewById(R.id.spinner_quantity).setOnClickListener(this);
        dialogView.findViewById(R.id.txt_date_expire).setOnClickListener(this);

        dialogView.findViewById(R.id.txt_cancel).setOnClickListener(this);
        dialogView.findViewById(R.id.txt_save).setOnClickListener(this);
        mDb = AppDatabase.getInstance(getContext(), AppExecutors.getInstance());

        init();

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

        return builder.create();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.spinner_quantity) {
            showNumberPicker(currentQuantity);
        } else if (view.getId() == R.id.txt_date_expire) {
            dateChooser();

            new DatePickerDialog(getContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        } else if (view.getId() == R.id.txt_cancel) {
            dismiss();
        } else if (view.getId() == R.id.txt_save) {
            if(listener  != null) {
                DialogMedicineFragment.this.getDialog().cancel();

                MedicineEntry medicineEntry = new MedicineEntry(
                        "CATEGORY",
                        mName.getText().toString(),
                        myCalendar.getTime(),
                        Integer.parseInt(mQuantity.getText().toString()),
                        new Date());

                listener.saveMedicine(medicineEntry);
            }
        }
    }

    private void init() {
        mQuantity = dialogView.findViewById(R.id.spinner_quantity);
        mName = dialogView.findViewById(R.id.txt_name_medicine);

        mDay = dialogView.findViewById(R.id.day_number_text);
        mMonth = dialogView.findViewById(R.id.month_text);
        mYear = dialogView.findViewById(R.id.year_text);

        if (mMedicineId != DEFAULT_TASK_ID) {
            final MedsViewModel viewModel
                    = ViewModelProviders.of(this).get(MedsViewModel.class);
            viewModel.load(mMedicineId).observe(this, new Observer<MedicineEntry>() {
                @Override
                public void onChanged(@Nullable MedicineEntry medicineEntry) {
                    viewModel.load(mMedicineId).removeObserver(this);
                    populateUI(medicineEntry);
                }
            });
        } else {
            myCalendar = Calendar.getInstance();

            mDay.setText(String.valueOf(myCalendar.get(Calendar.DAY_OF_MONTH)));
            mMonth.setText(myCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
            mYear.setText(String.valueOf(myCalendar.get(Calendar.YEAR)));
        }
    }

    private void populateUI(MedicineEntry medicineEntry) {
        if (medicineEntry == null) {
            return;
        }

        mName.setText(medicineEntry.getName());
        mQuantity.setText(String.valueOf(medicineEntry.getQuantity()));

        myCalendar = Calendar.getInstance();
        myCalendar.setTime(medicineEntry.getExpireAt());

        mDay.setText(String.valueOf(myCalendar.get(Calendar.DAY_OF_MONTH)));
        mMonth.setText(myCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
        mYear.setText(String.valueOf(myCalendar.get(Calendar.YEAR)));
    }

    public interface OnDialogMedicineListener {
        void saveMedicine(MedicineEntry medicineEntry);
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
        mDay.setText(String.valueOf(myCalendar.get(Calendar.DAY_OF_MONTH)));
        mMonth.setText(myCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
        mYear.setText(String.valueOf(myCalendar.get(Calendar.YEAR)));
    }

    private void showNumberPicker() {
        showNumberPicker(currentQuantity);
    }

    private void showNumberPicker(int value) {
        RelativeLayout linearLayout = new RelativeLayout(getContext());
        final NumberPicker aNumberPicker = new NumberPicker(getContext());
        aNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        aNumberPicker.setMaxValue(10);
        aNumberPicker.setValue(value);
        aNumberPicker.setMinValue(1);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(aNumberPicker,numPicerParams);

        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("Seleziona la quantità");
        alertDialogBuilder.setView(linearLayout);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                currentQuantity = aNumberPicker.getValue();
                                mQuantity.setText(String.valueOf(aNumberPicker.getValue()));
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
