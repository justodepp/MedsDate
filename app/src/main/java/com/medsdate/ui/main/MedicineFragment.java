package com.medsdate.ui.main;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.medsdate.R;
import com.medsdate.data.db.AppDatabase;
import com.medsdate.data.db.model.MedicineEntry;
import com.medsdate.databinding.MainFragmentBinding;
import com.medsdate.ui.viewmodel.MedicineListViewModel;
import com.medsdate.utils.AppExecutors;

import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MedicineFragment extends Fragment {

    public static final String TAG = "MedicineListViewModel";

    private MedicineListViewModel mViewModel;

    private AppDatabase mDb;

    private RecyclerView mRecyclerView;
    private MedsAdapter mAdapter;

    private MainFragmentBinding mBinding;

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
        //return inflater.inflate(R.layout.main_fragment, container, false);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDb = AppDatabase.getInstance(getActivity().getApplicationContext(), AppExecutors.getInstance());

        mViewModel = ViewModelProviders.of(this).get(MedicineListViewModel.class);
        // Set the RecyclerView to its corresponding view
        mRecyclerView = mBinding.rvMain;

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new MedsAdapter(mMedicineClickCallback);
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

        subscribeUi(mViewModel.getMeds());
    }

    private void subscribeUi(LiveData<List<MedicineEntry>> liveData) {
        // Update the list when the data changes
        liveData.observe(this, new Observer<List<MedicineEntry>>() {
            @Override
            public void onChanged(@Nullable List<MedicineEntry> myMeds) {
                if (myMeds != null) {
                    mBinding.setIsLoading(false);
                    mAdapter.setMedsList(myMeds);
                } else {
                    mBinding.setIsLoading(true);
                }
                // espresso does not know how to wait for data binding's loop so we execute changes
                // sync.
                mBinding.executePendingBindings();
            }
        });
    }
}

/*
DialogWelcomeFragment.newInstance(true, true, new DialogWelcomeFragment.OnDialogWelcomeListener() {
@Override
public void startTrial() {
        Intent tutorialIntent = new Intent(OptionActivity.this, TrialWizardActivity.class);
        tutorialIntent.putExtra("url", String.format(ApiUtil.getInstance().trialUrl(),
        UUIDHandler.getDeviceId(getApplicationContext())));
        startActivity(tutorialIntent);
        }

@Override
public void startAccedi() {
        Intent loginIntent = new Intent(OptionActivity.this, LoginActivity.class);
        loginIntent.putExtra(LoginActivity.LOGIN_FORM_NEEDED, true);
        startActivity(loginIntent);
        }
        }).show(getSupportFragmentManager(), "DialogWelcomeFragment");*/