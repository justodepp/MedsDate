package com.medsdate.ui.main;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.medsdate.R;
import com.medsdate.data.db.model.MedicineEntry;
import com.medsdate.databinding.ItemRvMainBinding;

import java.util.List;
import java.util.Objects;

public class MedsAdapter extends RecyclerView.Adapter<MedsAdapter.MedicineViewHolder> {

    List<? extends MedicineEntry> mMedsList;

    @Nullable
    private final MedicineClickCallback mMedicineClickCallback;

    public MedsAdapter(@Nullable MedicineClickCallback clickCallback) {
        mMedicineClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setMedsList(final List<? extends MedicineEntry> medsList) {
        if (mMedsList == null) {
            mMedsList = medsList;
            notifyItemRangeInserted(0, medsList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mMedsList.size();
                }

                @Override
                public int getNewListSize() {
                    return medsList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mMedsList.get(oldItemPosition).getId() ==
                            medsList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    MedicineEntry newProduct = medsList.get(newItemPosition);
                    MedicineEntry oldProduct = mMedsList.get(oldItemPosition);
                    return newProduct.getId() == oldProduct.getId()
                            && Objects.equals(newProduct.getName(), oldProduct.getName())
                            && Objects.equals(newProduct.getExpireAt(), oldProduct.getExpireAt());
                }
            });
            mMedsList = medsList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public MedicineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemRvMainBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.item_rv_main,
                        parent, false);
        binding.setCallback(mMedicineClickCallback);
        return new MedicineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MedicineViewHolder holder, int position) {
        holder.binding.setMedicine(mMedsList.get(position));
        holder.binding.executePendingBindings();
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

        final ItemRvMainBinding binding;

        public MedicineViewHolder(ItemRvMainBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public List<MedicineEntry> getMeds() {
        return (List<MedicineEntry>) mMedsList;
    }
}
