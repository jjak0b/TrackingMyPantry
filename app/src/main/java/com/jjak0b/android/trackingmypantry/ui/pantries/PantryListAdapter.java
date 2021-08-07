package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.model.Pantry;

public class PantryListAdapter extends ListAdapter<Pantry, PantryViewHolder> {
    private FragmentManager fm;
    private String productID;

    protected PantryListAdapter(@NonNull DiffUtil.ItemCallback<Pantry> diffCallback, FragmentManager fm, String productID ) {
        super(diffCallback);
        this.fm = fm;
        this.productID = productID;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PantryViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        Pantry current = getItem(position);
        holder.bind(current, productID, fm);
    }

    static class ProductDiff extends DiffUtil.ItemCallback<Pantry> {
        @Override
        public boolean areItemsTheSame(@NonNull Pantry oldItem, @NonNull Pantry newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Pantry oldItem, @NonNull Pantry newItem) {
            return oldItem.getId() != newItem.getId();
        }
    }
}