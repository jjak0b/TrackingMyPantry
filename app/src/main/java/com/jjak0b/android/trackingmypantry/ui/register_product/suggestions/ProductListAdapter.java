package com.jjak0b.android.trackingmypantry.ui.register_product.suggestions;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;

import java.util.Objects;

public class ProductListAdapter extends ListAdapter<UserProduct, ProductViewHolder> {

    private ProductViewHolder.OnProductClickListener onProductClickListener;

    protected ProductListAdapter(@NonNull DiffUtil.ItemCallback<UserProduct> diffCallback, ProductViewHolder.OnProductClickListener onProductClickListener ) {
        super(diffCallback);
        this.onProductClickListener = onProductClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ProductViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        UserProduct current = getItem(position);
        holder.bind(current, onProductClickListener);
    }

    static class ProductDiff extends DiffUtil.ItemCallback<UserProduct> {
        @Override
        public boolean areItemsTheSame(@NonNull UserProduct oldItem, @NonNull UserProduct newItem) {
            return Objects.equals( oldItem.getRemote_id(), newItem.getRemote_id() );
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserProduct oldItem, @NonNull UserProduct newItem) {
            return Objects.equals( oldItem, newItem );
        }
    }
}