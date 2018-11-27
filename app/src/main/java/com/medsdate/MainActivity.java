package com.medsdate;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.medsdate.data.db.AppDatabase;
import com.medsdate.data.db.model.MedicineEntry;
import com.medsdate.ui.dialogs.DialogMedicineFragment;
import com.medsdate.ui.main.MedsAdapter;
import com.medsdate.ui.viewmodel.MedsViewModel;
import com.medsdate.utils.AppExecutors;
import com.medsdate.utils.ItemClickSupport;

import java.util.List;

import timber.log.Timber;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MedsViewModel mViewModel;
    private AppDatabase mDb;

    private MedsAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        findViewById(R.id.fab).setOnClickListener(this);
        mViewModel = ViewModelProviders.of(this).get(MedsViewModel.class);
        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.rv_main);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new MedsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(this, VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                int position = viewHolder.getAdapterPosition();
                List<MedicineEntry> medicine = mAdapter.getMeds();
                mViewModel.delete(medicine.get(position));

            }
        }).attachToRecyclerView(mRecyclerView);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                List<MedicineEntry> medicine = mAdapter.getMeds();

                DialogMedicineFragment.newInstance(true, medicine.get(position).getId(),
                        new DialogMedicineFragment.OnDialogMedicineListener() {
                    @Override
                    public void saveMedicine(MedicineEntry medicineEntry) {
                    }

                    @Override
                    public void updateMedicine(MedicineEntry medicineEntry) {
                        mViewModel.update(medicineEntry);
                    }
                }).show(getSupportFragmentManager(), "DialogMedicineFragment");
            }
        });

        mDb = AppDatabase.getInstance(this, AppExecutors.getInstance());
        setupViewModel();
    }

    private void setupViewModel() {
        mViewModel.getMeds().observe(this, new Observer<List<MedicineEntry>>() {
            @Override
            public void onChanged(@Nullable List<MedicineEntry> medsEntries) {
                Timber.d("Updating list of Meds from LiveData in ViewModel");
                mAdapter.setMeds(medsEntries);
                if(medsEntries.size() > 0) {
                    findViewById(R.id.empty_view).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            DialogMedicineFragment.newInstance(false, new DialogMedicineFragment.OnDialogMedicineListener() {
                @Override
                public void saveMedicine(MedicineEntry medicineEntry) {
                    mViewModel.insert(medicineEntry);
                }

                @Override
                public void updateMedicine(MedicineEntry medicineEntry) {
                    return;
                }
            }).show(getSupportFragmentManager(), "DialogMedicineFragment");
        }
    }
}
