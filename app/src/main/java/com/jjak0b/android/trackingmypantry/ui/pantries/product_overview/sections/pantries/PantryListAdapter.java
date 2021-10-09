package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries;

import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;

import java.util.Objects;

public class PantryListAdapter extends ListAdapter<PantryWithProductInstanceGroups, PantryViewHolder> {
    private PantryInteractionsListener interactionsListener;

    protected PantryListAdapter(@NonNull DiffUtil.ItemCallback<PantryWithProductInstanceGroups> diffCallback, PantryInteractionsListener listener ) {
        super(diffCallback);
        this.interactionsListener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).pantry.getId();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PantryViewHolder viewHolder = PantryViewHolder.create(parent);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryViewModel viewModel = new ViewModelProvider((ViewModelStoreOwner) holder.itemView.getContext())
                .get( PantryViewModel.class.getName() + getItemId(position), PantryViewModel.class );
        viewModel.setItem(getItem(position));
        viewModel.setInteractionsListener(interactionsListener);
        holder.bindTo(viewModel);
    }

    static class ProductDiff extends DiffUtil.ItemCallback<PantryWithProductInstanceGroups> {
        @Override
        public boolean areItemsTheSame(@NonNull PantryWithProductInstanceGroups oldItem, @NonNull PantryWithProductInstanceGroups newItem) {
            boolean isSame = oldItem.pantry.getId() == newItem.pantry.getId();
            return  isSame;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryWithProductInstanceGroups oldItem, @NonNull PantryWithProductInstanceGroups newItem) {
            boolean isSame = Objects.equals( oldItem, newItem );
            return  isSame;
        }
    }
}