package com.medsdate

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.google.android.material.snackbar.Snackbar
import com.medsdate.billing.BillingHandler
import com.medsdate.billing.BillingHandler.BillingCallbacks
import com.medsdate.data.db.model.MedicineEntry
import com.medsdate.notification.Receiver.Companion.cancelAlarmNotification
import com.medsdate.notification.Receiver.Companion.setAlarmNotification
import com.medsdate.ui.dialogs.BottomSheetDialogMedicineFragment
import com.medsdate.ui.dialogs.DialogCreditsFragment
import com.medsdate.ui.main.MedsAdapter
import com.medsdate.ui.viewmodel.MedsViewModel
import com.medsdate.utils.Utility.getSpan
import com.medsdate.utils.extensions.recycleOnItemClicks
import timber.log.Timber
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener, BottomSheetDialogMedicineFragment.OnDialogMedicineListener, BillingCallbacks {

    private var mViewModel: MedsViewModel? = null
    private var mAdapter: MedsAdapter? = null
    private var mBilling: BillingHandler? = null
    private var mHideSnackbar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        //Billing
        mBilling = BillingHandler(this, this, SKU)
        findViewById<View>(R.id.fab).setOnClickListener(this)
        mViewModel = ViewModelProviders.of(this).get(MedsViewModel::class.java)
        // Set the RecyclerView to its corresponding view
        val mRecyclerView = findViewById<RecyclerView>(R.id.rv_main)
        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.layoutManager = GridLayoutManager(this, getSpan(this))
        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = MedsAdapter()
        mRecyclerView.adapter = mAdapter
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        mRecyclerView.addItemDecoration(decoration)
        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            // Called when a user swipes left or right on a ViewHolder
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) { // Here is where you'll implement swipe to delete
                val position = viewHolder.adapterPosition
                val medicine = mAdapter!!.meds
                medicine?.get(position)?.let { cancelAlarmNotification(this@MainActivity, it) }
                mViewModel!!.delete(medicine?.get(position))
            }
        }).attachToRecyclerView(mRecyclerView)
        /*ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener { recyclerView, position, v ->
            val medicine = mAdapter!!.meds
            *//*DialogMedicineFragment.newInstance(true, medicine.get(position).getId(),
                        MainActivity.this)
                        .show(getSupportFragmentManager(), "DialogMedicineFragment");*//*
            medicine?.get(position)?.id?.let {
                BottomSheetDialogMedicineFragment.newInstance(true, it,
                    this@MainActivity)
                    .show(supportFragmentManager, "DialogMedicineFragment")
            }
        }*/

        mRecyclerView.recycleOnItemClicks { position, view ->
            val medicine = mAdapter!!.meds
            /*DialogMedicineFragment.newInstance(true, medicine.get(position).getId(),
                        MainActivity.this)
                        .show(getSupportFragmentManager(), "DialogMedicineFragment");*/
            medicine?.get(position)?.id?.let {
                BottomSheetDialogMedicineFragment.newInstance(true, it,
                        this@MainActivity)
                        .show(supportFragmentManager, "DialogMedicineFragment")
            }
        }
        setupViewModel()
    }

    private fun setupViewModel() {
        mViewModel!!.meds.observe(this, Observer<List<MedicineEntry?>?> { medsEntries ->
            Timber.d("Updating list of Meds from LiveData in ViewModel")
            mAdapter!!.meds = medsEntries as List<MedicineEntry>?
            if (medsEntries!!.isNotEmpty()) {
                findViewById<View>(R.id.empty_view).visibility = View.GONE
            } else {
                findViewById<View>(R.id.empty_view).visibility = View.VISIBLE
            }
            presentShowcaseSequence()
        })
    }

    //region Click
    override fun onClick(view: View) {
        if (view.id == R.id.fab) { /*DialogMedicineFragment.newInstance(false, this)
                        .show(getSupportFragmentManager(), "DialogMedicineFragment");*/
            BottomSheetDialogMedicineFragment.newInstance(false,
                    this@MainActivity)
                    .show(supportFragmentManager, "DialogMedicineFragment")
        }
    }

    override fun saveMedicine(medicineEntry: MedicineEntry) {
        mViewModel!!.insert(medicineEntry)
        setAlarmNotification(this, medicineEntry)
    }

    override fun updateMedicine(medicineEntry: MedicineEntry) {
        mViewModel!!.update(medicineEntry)
        setAlarmNotification(this, medicineEntry)
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.text_med_updated), Snackbar.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        showAlertWithMessage("Are you sure to exit?")
    }

    //endregion

    //region Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle item selection
        return when (item.itemId) {
            R.id.action_credits -> {
                DialogCreditsFragment.newInstance()
                        .show(supportFragmentManager, "DialogCreditsFragment")
                true
            }
            R.id.action_donate -> {
                showDonation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //endregion

    //region Billing
    private fun showDonation() {
        val builder = AlertDialog.Builder(this)
                .setTitle(R.string.donate_title)
                .setMessage(R.string.donate_desc)
                .setNeutralButton(android.R.string.cancel, null)
        val skus: List<SkuDetails> = ArrayList(skus)
        Collections.sort(skus) { o1, o2 -> java.lang.Long.compare(o1.priceAmountMicros, o2.priceAmountMicros) }
        var i = 0
        while (i < skus.size && i < 2) {
            val sku = skus[i]
            val price = sku.price
            val listener = DialogInterface.OnClickListener { dialog, which -> buy(sku) }
            if (i == 0) { // Cheapest IAP
                builder.setNegativeButton(price, listener)
            } else { // More expensive IAP
                builder.setPositiveButton(price, listener)
            }
            i++
        }
        builder.create().show()
    }

    private fun buy(sku: SkuDetails) {
        mBilling!!.buy(sku)
    }

    private val skus: Collection<SkuDetails>
        get() = mBilling!!.skus

    override fun onStateChanged(connected: Boolean) {
        Timber.w("onStateChanged $connected")
    }

    override fun onPurchased(sku: SkuDetails?, isNew: Boolean) {
        Timber.w("onPurchased " + sku!!.sku + " " + isNew)
        if (!isNew) {
            mHideSnackbar = if (mHideSnackbar) {
                return
            } else {
                true
            }
        }
        Snackbar.make(findViewById(android.R.id.content),
                if (isNew) R.string.donate_new else R.string.donate_history,
                Snackbar.LENGTH_LONG).show()
    }

    //endregion

    //region System
    override fun onDestroy() {
        mBilling!!.destroy()
        super.onDestroy()
    }
    //endregion

    private fun showAlertWithMessage(message: String?) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(R.string.app_name)
        builder.setMessage(message)
        builder.setPositiveButton("Ok") { _, _ -> finish() }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun presentShowcaseSequence() {
        val config = ShowcaseConfig()
        config.delay = 500 // half second between each showcase view
        val sequence = MaterialShowcaseSequence(this, SHOWCASE_ID)
        sequence.setConfig(config)
        sequence.addSequenceItem(
                MaterialShowcaseView.Builder(this)
                        .setMaskColour(resources.getColor(R.color.showcase_mask_color))
                        .setTarget(findViewById(R.id.empty_shelter_image))
                        .setContentText(getString(R.string.showcase_drugs))
                        .setDismissText(getString(R.string.showcase_gotit))
                        .useFadeAnimation()
                        .build()
        )
        sequence.addSequenceItem(
                MaterialShowcaseView.Builder(this)
                        .setMaskColour(resources.getColor(R.color.showcase_mask_color))
                        .setTarget(findViewById(R.id.fab))
                        .setContentText(getString(R.string.showcase_fab))
                        .setDismissText(getString(R.string.showcase_gotit))
                        .useFadeAnimation()
                        .build()
        )
        sequence.addSequenceItem(
                MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.empty_view))
                        .setMaskColour(resources.getColor(R.color.showcase_mask_color))
                        .setContentText(getString(R.string.showcase_wellcome_text,
                                getString(R.string.app_name)))
                        .setDismissText(getString(R.string.showcase_start))
                        .useFadeAnimation()
                        .withoutShape()
                        .build()
        )
        sequence.start()
    }

    companion object {
        private const val SHOWCASE_ID = "sequence.SHOWCASE_ID"
        private val SKU = listOf("donation.regular", "donation.large")
    }
}