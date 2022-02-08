package com.jjak0b.android.trackingmypantry.ui.register_product.suggestions;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;

import java.util.Objects;

public class ProductListAdapter<T extends Product> extends ListAdapter<T, ProductViewHolder<T>> {

    private ProductViewHolder.OnProductClickListener<T> onProductClickListener;

    protected ProductListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, ProductViewHolder.OnProductClickListener<T> onProductClickListener ) {
        super(diffCallback);
        this.onProductClickListener = onProductClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return ProductOwnedViewHolder.create(parent);
        }
        else {
            return ProductViewHolder.create(parent);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if( getItem(position) instanceof UserProduct ) {
            return 1;
        }
        else {
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder<T> holder, int position) {
        T current = getItem(position);
        holder.bind(current, onProductClickListener);
    }

    static class ProductDiff<T extends Product> extends DiffUtil.ItemCallback<T> {
        @Override
        public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            return Objects.equals( oldItem.getRemote_id(), newItem.getRemote_id() );
        }

        @Override
        public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            return Objects.equals( oldItem, newItem );
        }
    }
}