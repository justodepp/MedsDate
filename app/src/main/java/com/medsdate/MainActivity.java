package com.medsdate;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.medsdate.data.db.model.MedicineEntry;
import com.medsdate.ui.dialogs.DialogMedicineFragment;
import com.medsdate.ui.main.MedicineFragment;
import com.medsdate.ui.viewmodel.MedsViewModel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MedicineFragment.newInstance())
                    .commitNow();
        }

//        Set the Floating Action Button (FAB) to its corresponding View.
//        Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
//        to launch the AddTaskActivity.

        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogMedicineFragment.newInstance(false, true, new DialogMedicineFragment.OnDialogMedicineListener() {
                    @Override
                    public void saveMedicine(MedicineEntry medicineEntry) {
                        MedsViewModel mViewModel = ViewModelProviders.of(MainActivity.this).get(MedsViewModel.class);
                        mViewModel.insert(medicineEntry);
                    }

                    @Override
                    public void updateMedicine() {
                    }
                }).show(getSupportFragmentManager(), "DialogMedicineFragment");
            }
        });
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }
}
