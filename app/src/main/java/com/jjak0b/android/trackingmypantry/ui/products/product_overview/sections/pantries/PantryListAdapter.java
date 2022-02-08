package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.db.results.PantryDetails;

import java.util.Objects;

public class PantryListAdapter extends ListAdapter<PantryDetails, PantryViewHolder> {
    private PantryInteractionsListener interactionsListener;

    protected PantryListAdapter(@NonNull DiffUtil.ItemCallback<PantryDetails> diffCallback, PantryInteractionsListener listener ) {
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

    static class ProductDiff extends DiffUtil.ItemCallback<PantryDetails> {
        @Override
        public boolean areItemsTheSame(@NonNull PantryDetails oldItem, @NonNull PantryDetails newItem) {
            boolean isSame = oldItem.pantry == newItem.pantry;
            return  isSame;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryDetails oldItem, @NonNull PantryDetails newItem) {
            boolean isSame = Objects.equals( oldItem, newItem );
            return  isSame;
        }
    }
}