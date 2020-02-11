package com.medsdate.ui.main

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.medsdate.R
import com.medsdate.data.db.model.MedicineEntry
import com.medsdate.ui.main.MedsAdapter.MedicineViewHolder
import com.medsdate.utils.Utility.getDatePart
import com.medsdate.utils.Utility.loadImage
import java.text.SimpleDateFormat
import java.util.*

class MedsAdapter : RecyclerView.Adapter<MedicineViewHolder>() {
    private var mMedsList: List<MedicineEntry>? = null
    // Date formatter
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder { // Inflate the task_layout to a view
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rv_main, parent, false)
        return MedicineViewHolder(view)
        /*ItemRvMainBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.item_rv_main,
                        parent, false);
        binding.setCallback(mMedicineClickCallback);
        return new MedicineViewHolder(binding);*/
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) { //        holder.binding.setMedicine(mMedsList.get(position));
//        holder.binding.executePendingBindings();
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return if (mMedsList == null) 0 else mMedsList!!.size
    }

    override fun getItemId(position: Int): Long {
        return mMedsList!![position].id.toLong()
    }

    inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mCategory: TextView
        var mTitle: TextView
        var mQuantity: TextView
        var mDay: TextView
        var mMonth: TextView
        var mYear: TextView
        var mImage: ImageView
        fun bind(position: Int) {
            val medicineEntry = mMedsList!![position]
            mCategory.text = medicineEntry.category
            mTitle.text = medicineEntry.name
            mQuantity.text = medicineEntry.quantity.toString()
            if (getDatePart(medicineEntry.expireAt!!) == getDatePart(Calendar.getInstance().time)) {
                itemView.findViewById<View>(R.id.img_expired).visibility = View.VISIBLE
            } else {
                itemView.findViewById<View>(R.id.img_expired).visibility = View.GONE
            }
            val myCalendar = Calendar.getInstance()
            myCalendar.time = medicineEntry.expireAt
            mDay.text = myCalendar[Calendar.DAY_OF_MONTH].toString()
            mMonth.text = myCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
            mYear.text = myCalendar[Calendar.YEAR].toString()
            if (medicineEntry.image != "") {
                Glide.with(itemView.context)
                        .asDrawable()
                        .load(loadImage(itemView.context, medicineEntry.image!!))
                        .into(mImage)
                mImage.imageTintList = ColorStateList.valueOf(itemView.context.resources.getColor(android.R.color.transparent))
            } else {
                Glide.with(itemView.context)
                        .load(R.drawable.ic_add_photo)
                        .into(mImage)
                mImage.imageTintList = ColorStateList.valueOf(itemView.context.resources.getColor(R.color.colorAccent))
            }
        }

        init {
            //            super(binding.getRoot());
//            this.binding = binding;
            mCategory = itemView.findViewById(R.id.category_text)
            mTitle = itemView.findViewById(R.id.title_text)
            mQuantity = itemView.findViewById(R.id.quantity_text)
            mDay = itemView.findViewById(R.id.day_number_text)
            mMonth = itemView.findViewById(R.id.month_text)
            mYear = itemView.findViewById(R.id.year_text)
            mImage = itemView.findViewById(R.id.imageView)
        }
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    var meds: List<MedicineEntry>?
        get() = mMedsList
        set(medsEntries) {
            mMedsList = medsEntries
            notifyDataSetChanged()
        }

    override fun toString(): String {
        return super.toString()
    }

    companion object {
        // Constant for date format
        private const val DATE_FORMAT = "dd/MM/yyy"
    }

    //    @Nullable
//    private final MedicineClickCallback mMedicineClickCallback;
    init { //        mMedicineClickCallback = clickCallback;
        setHasStableIds(true)
    }
}