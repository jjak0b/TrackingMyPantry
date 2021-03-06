package com.jjak0b.android.trackingmypantry.ui.products;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;

import java.util.Objects;

public class ProductListAdapter extends ListAdapter<ProductWithTags, ProductViewHolder> {

    private OnProductClick onClickListener;

    protected ProductListAdapter(@NonNull DiffUtil.ItemCallback<ProductWithTags> diffCallback, OnProductClick onClickListener ) {
        super(diffCallback);
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ProductViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductWithTags current = getItem(position);
        holder.bind(current);
        holder.itemView.setOnClickListener(v -> onClickListener.onAction(current.product) );
    }

    static class ProductDiff extends DiffUtil.ItemCallback<ProductWithTags> {
        @Override
        public boolean areItemsTheSame(@NonNull ProductWithTags oldItem, @NonNull ProductWithTags newItem) {
            return Objects.equals(  oldItem.product.getBarcode(), newItem.product.getBarcode() );
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProductWithTags oldItem, @NonNull ProductWithTags newItem) {
            return Objects.equals( oldItem.product, newItem.product )
                    && Objects.equals( oldItem.tags, newItem.tags);
        }
    }

    public interface OnProductClick {
        void onAction(UserProduct p);
    }
}