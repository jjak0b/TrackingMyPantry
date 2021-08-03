package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;

public class PantryListAdapter extends ListAdapter<PantryWithProductInstanceGroups, PantryViewHolder> {

    private PantriesBrowserViewModel viewModel;

    protected PantryListAdapter(@NonNull DiffUtil.ItemCallback<PantryWithProductInstanceGroups> diffCallback, @NonNull PantriesBrowserViewModel viewModel ) {
        super(diffCallback);
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PantryViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryWithProductInstanceGroups current = getItem(position);
        holder.bind(current, viewModel);
    }

    static class ProductDiff extends DiffUtil.ItemCallback<PantryWithProductInstanceGroups> {
        @Override
        public boolean areItemsTheSame(@NonNull PantryWithProductInstanceGroups oldItem, @NonNull PantryWithProductInstanceGroups newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryWithProductInstanceGroups oldItem, @NonNull PantryWithProductInstanceGroups newItem) {
            return oldItem.pantry.getId() != newItem.pantry.getId();
        }
    }


}