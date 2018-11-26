package com.medsdate.ui.main;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.medsdate.R;
import com.medsdate.data.db.AppDatabase;
import com.medsdate.data.db.model.MedicineEntry;
import com.medsdate.ui.dialogs.DialogMedicineFragment;
import com.medsdate.ui.viewmodel.MedsViewModel;
import com.medsdate.utils.AppExecutors;
import com.medsdate.utils.ItemClickSupport;

import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MedicineFragment extends Fragment {

    public static final String TAG = "MedsViewModel";

    private MedsViewModel mViewModel;

    private AppDatabase mDb;

    private RecyclerView mRecyclerView;
    private MedsAdapter mAdapter;

    private View mView;

    private final MedicineClickCallback mMedicineClickCallback = new MedicineClickCallback() {
        @Override
        public void onClick(MedicineEntry medicine) {

            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                //((MainActivity) getActivity()).show(medicine);
            }
        }
    };

    public static MedicineFragment newInstance() {
        return new MedicineFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
//        mBinding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false);
//
//        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(MedsViewModel.class);
        // Set the RecyclerView to its corresponding view
        mRecyclerView = mView.findViewById(R.id.rv_main);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new MedsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), VERTICAL);
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
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<MedicineEntry> medicine = mAdapter.getMeds();
                        mDb.medicineDao().deleteMedicine(medicine.get(position));
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                List<MedicineEntry> medicine = mAdapter.getMeds();

                DialogMedicineFragment.newInstance(true, true, new DialogMedicineFragment.OnDialogMedicineListener() {
                    @Override
                    public void saveMedicine() {
                    }

                    @Override
                    public void updateMedicine() {
                        mDb.medicineDao().updateMedicine(medicine.get(position));
                    }
                }).show(getActivity().getSupportFragmentManager(), "DialogMedicineFragment");
            }
        });

        mDb = AppDatabase.getInstance(getContext(), AppExecutors.getInstance());
        setupViewModel();
    }

    private void setupViewModel() {
        mViewModel.getMeds().observe(this, new Observer<List<MedicineEntry>>() {
            @Override
            public void onChanged(@Nullable List<MedicineEntry> medsEntries) {
                Log.d(TAG, "Updating list of Meds from LiveData in ViewModel");
                mAdapter.setMeds(medsEntries);
            }
        });
    }
}