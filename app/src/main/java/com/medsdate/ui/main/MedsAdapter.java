package com.medsdate.ui.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.medsdate.R;
import com.medsdate.data.db.model.MedicineEntry;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MedsAdapter extends RecyclerView.Adapter<MedsAdapter.MedicineViewHolder> {

    List<? extends MedicineEntry> mMedsList;

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

    static class MedicineViewHolder extends RecyclerView.ViewHolder {

        public MedicineViewHolder(View itemView) {
            super(itemView);
//            super(binding.getRoot());
//            this.binding = binding;
        }

        public void bind(int position){

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
