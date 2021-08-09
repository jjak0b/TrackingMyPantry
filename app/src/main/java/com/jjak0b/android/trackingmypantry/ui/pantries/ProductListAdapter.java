package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;

import java.util.List;
import java.util.Objects;

public class ProductListAdapter extends ListAdapter<ProductWithTags, ProductViewHolder> {

    private ProductsBrowserViewModel viewModel;
    private FragmentManager fm;
    private LifecycleRegistry lifecycleRegistry;

    protected ProductListAdapter(@NonNull DiffUtil.ItemCallback<ProductWithTags> diffCallback, @NonNull ProductsBrowserViewModel viewModel, FragmentManager fm ) {
        super(diffCallback);
        this.viewModel = viewModel;
        this.fm = fm;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ProductViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductWithTags current = getItem(position);
        holder.bind(current, viewModel, fm );
    }

    static class ProductDiff extends DiffUtil.ItemCallback<ProductWithTags> {
        @Override
        public boolean areItemsTheSame(@NonNull ProductWithTags oldItem, @NonNull ProductWithTags newItem) {
            return Objects.equals(  oldItem.product.getId(), newItem.product.getId() );
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProductWithTags oldItem, @NonNull ProductWithTags newItem) {
            return Objects.equals( oldItem.product.getId(), newItem.product.getId())
                    && Objects.equals( oldItem.tags, newItem.tags);
        }
    }
}