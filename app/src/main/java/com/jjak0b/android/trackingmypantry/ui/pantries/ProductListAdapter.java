package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;

public class ProductListAdapter extends ListAdapter<ProductWithTags, ProductViewHolder> {

    private ProductsBrowserViewModel viewModel;
    private FragmentManager fm;

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
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProductWithTags oldItem, @NonNull ProductWithTags newItem) {
            return oldItem.product.getId() != newItem.product.getId();
        }
    }


}