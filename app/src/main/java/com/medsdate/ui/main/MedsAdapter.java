package com.medsdate.ui.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.medsdate.R;
import com.medsdate.data.db.model.MedicineEntry;
import com.medsdate.utils.GlideApp;
import com.medsdate.utils.Utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MedsAdapter extends RecyclerView.Adapter<MedsAdapter.MedicineViewHolder> {

    private List<? extends MedicineEntry> mMedsList;

    // Constant for date format
    private static final String DATE_FORMAT = "dd/MM/yyy";
    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

//    @Nullable
//    private final MedicineClickCallback mMedicineClickCallback;

    public MedsAdapter() {
//        mMedicineClickCallback = clickCallback;
        setHasStableIds(true);
    }

    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rv_main, parent, false);

        return new MedicineViewHolder(view);
        /*ItemRvMainBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.item_rv_main,
                        parent, false);
        binding.setCallback(mMedicineClickCallback);
        return new MedicineViewHolder(binding);*/
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
//        holder.binding.setMedicine(mMedsList.get(position));
//        holder.binding.executePendingBindings();

        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mMedsList == null ? 0 : mMedsList.size();
    }

    @Override
    public long getItemId(int position) {
        return mMedsList.get(position).getId();
    }

    public class MedicineViewHolder extends RecyclerView.ViewHolder {

        TextView mCategory;
        TextView mTitle;
        TextView mQuantity;
        TextView mDay;
        TextView mMonth;
        TextView mYear;
        ImageView mImage;

        public MedicineViewHolder(View itemView) {
            super(itemView);
//            super(binding.getRoot());
//            this.binding = binding;
            mCategory = itemView.findViewById(R.id.category_text);
            mTitle = itemView.findViewById(R.id.title_text);
            mQuantity = itemView.findViewById(R.id.quantity_text);
            mDay = itemView.findViewById(R.id.day_number_text);
            mMonth = itemView.findViewById(R.id.month_text);
            mYear = itemView.findViewById(R.id.year_text);
            mImage = itemView.findViewById(R.id.imageView);
        }

        public void bind(int position){
            MedicineEntry medicineEntry = mMedsList.get(position);
            mCategory.setText(medicineEntry.getCategory());
            mTitle.setText(medicineEntry.getName());
            mQuantity.setText(String.valueOf(medicineEntry.getQuantity()));

            Calendar myCalendar = Calendar.getInstance();
            myCalendar.setTime(medicineEntry.getExpireAt());

            mDay.setText(String.valueOf(myCalendar.get(Calendar.DAY_OF_MONTH)));
            mMonth.setText(myCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
            mYear.setText(String.valueOf(myCalendar.get(Calendar.YEAR)));

            if(!medicineEntry.getImage().equals("")){
                GlideApp.with(itemView.getContext())
                        .asDrawable()
                        .load(Utility.loadImage(itemView.getContext(), medicineEntry.getImage()))
                        .into(mImage);

                mImage.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.transparent));
            }
        }
    }

    public List<MedicineEntry> getMeds() {
        return (List<MedicineEntry>) mMedsList;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setMeds(List<MedicineEntry> medsEntries) {
        mMedsList = medsEntries;
        notifyDataSetChanged();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
