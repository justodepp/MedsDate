package com.medsdate;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.billingclient.api.SkuDetails;
import com.medsdate.billing.BillingHandler;
import com.medsdate.data.db.model.MedicineEntry;
import com.medsdate.notification.Receiver;
import com.medsdate.ui.dialogs.DialogCreditsFragment;
import com.medsdate.ui.dialogs.DialogMedicineFragment;
import com.medsdate.ui.main.MedsAdapter;
import com.medsdate.ui.viewmodel.MedsViewModel;
import com.medsdate.utils.ItemClickSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DialogMedicineFragment.OnDialogMedicineListener,
    BillingHandler.BillingCallbacks{

    private MedsViewModel mViewModel;

    private MedsAdapter mAdapter;

    private BillingHandler mBilling;
    private static final List<String> SKU = Arrays.asList("donation.regular", "donation.large");
    private boolean mHideSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //Billing
        //TODO: riabilitare appena verrà creata l'app sullo store
        mBilling = new BillingHandler(this, this, SKU);

        findViewById(R.id.fab).setOnClickListener(this);
        mViewModel = ViewModelProviders.of(this).get(MedsViewModel.class);
        // Set the RecyclerView to its corresponding view
        RecyclerView mRecyclerView = findViewById(R.id.rv_main);

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

                Receiver.cancelAlarmNotification(MainActivity.this, medicine.get(position));
                mViewModel.delete(medicine.get(position));
            }
        }).attachToRecyclerView(mRecyclerView);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                List<MedicineEntry> medicine = mAdapter.getMeds();

                DialogMedicineFragment.newInstance(true, medicine.get(position).getId(),
                        MainActivity.this)
                        .show(getSupportFragmentManager(), "DialogMedicineFragment");
            }
        });

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
            DialogMedicineFragment.newInstance(false, this)
                    .show(getSupportFragmentManager(), "DialogMedicineFragment");
        }
    }

    @Override
    public void saveMedicine(MedicineEntry medicineEntry) {
        mViewModel.insert(medicineEntry);
        Receiver.setAlarmNotification(this, medicineEntry);
    }

    @Override
    public void updateMedicine(MedicineEntry medicineEntry) {
        mViewModel.update(medicineEntry);
        Receiver.setAlarmNotification(this, medicineEntry);
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.text_med_updated), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        showAlertWithMessage("Are you sure to exit?");
    }

    public void showAlertWithMessage(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_credits:
                DialogCreditsFragment.newInstance()
                        .show(getSupportFragmentManager(), "DialogCreditsFragment");
                return true;
            case R.id.action_donate:
                showDonation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        mBilling.destroy();
        super.onDestroy();
    }

    private void showDonation(){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(R.string.donate_title)
                .setMessage(R.string.donate_desc)
                .setNeutralButton(android.R.string.cancel, null);

        List<SkuDetails> skus = new ArrayList<>(getSkus());
        Collections.sort(skus, new Comparator<SkuDetails>() {
            @Override
            public int compare(SkuDetails o1, SkuDetails o2) {
                return Long.compare(o1.getPriceAmountMicros(), o2.getPriceAmountMicros());
            }
        });

        for (int i = 0; i < skus.size() && i < 2; i++) {
            final SkuDetails sku = skus.get(i);

            String price = sku.getPrice();
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    buy(sku);
                }
            };

            if (i == 0) {
                // Cheapest IAP
                builder.setNegativeButton(price, listener);
            } else {
                // More expensive IAP
                builder.setPositiveButton(price, listener);
            }
        }
        builder.create().show();
    }

    private void buy(SkuDetails sku) {
        mBilling.buy(sku);
    }

    private Collection<SkuDetails> getSkus() {
        return mBilling.getSkus();
    }

    @Override
    public void onStateChanged(boolean connected) {
        Timber.w("onStateChanged " + connected);
    }

    @Override
    public void onPurchased(SkuDetails sku, boolean isNew) {
        Timber.w("onPurchased " + sku.getSku() + " " + isNew);
        if (!isNew) {
            if (mHideSnackbar) {
                return;
            } else {
                mHideSnackbar = true;
            }
        }
        Snackbar.make(findViewById(android.R.id.content),
                isNew ? R.string.donate_new : R.string.donate_history,
                Snackbar.LENGTH_LONG).show();
    }
}
