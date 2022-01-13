package com.jjak0b.android.trackingmypantry.ui.register_product.suggestions;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.db.entities.Product;

import java.util.Objects;

public class ProductListAdapter extends ListAdapter<Product, ProductViewHolder> {

    private ProductViewHolder.OnProductClickListener onProductClickListener;

    protected ProductListAdapter(@NonNull DiffUtil.ItemCallback<Product> diffCallback, ProductViewHolder.OnProductClickListener onProductClickListener ) {
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
        Product current = getItem(position);
        holder.bind(current, onProductClickListener);
    }

    static class ProductDiff extends DiffUtil.ItemCallback<Product> {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return Objects.equals( oldItem.getRemote_id(), newItem.getRemote_id() );
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return Objects.equals( oldItem, newItem );
        }
    }
}