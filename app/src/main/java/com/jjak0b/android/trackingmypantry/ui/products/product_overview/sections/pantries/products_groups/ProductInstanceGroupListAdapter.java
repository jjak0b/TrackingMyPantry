package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.model.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups.holder.ProductInstanceGroupViewHolder;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups.model.ProductInstanceGroupInteractionsListener;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups.model.ProductInstanceGroupViewModel;

import java.util.Objects;

public abstract class ProductInstanceGroupListAdapter extends ListAdapter<ProductInstanceGroup, ProductInstanceGroupViewHolder> implements ViewModelStoreOwner {
    private ProductInstanceGroupInteractionsListener interactionsListener;

    protected ProductInstanceGroupListAdapter(@NonNull DiffUtil.ItemCallback<ProductInstanceGroup> diffCallback, ProductInstanceGroupInteractionsListener listener ) {
        super(diffCallback);
        this.interactionsListener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @NonNull
    @Override
    public ProductInstanceGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProductInstanceGroupViewHolder viewHolder = ProductInstanceGroupViewHolder.create(parent);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductInstanceGroupViewHolder holder, int position) {

        ProductInstanceGroupViewModel viewModel = new ViewModelProvider(this)
                .get( ProductInstanceGroupViewModel.class.getName() + getItemId(position), ProductInstanceGroupViewModel.class );
        viewModel.setItem(getItem(position));
        viewModel.setInteractionsListener(interactionsListener);
        holder.bindTo(viewModel);
    }

    static class ProductDiff extends DiffUtil.ItemCallback<ProductInstanceGroup> {
        @Override
        public boolean areItemsTheSame(@NonNull ProductInstanceGroup oldItem, @NonNull ProductInstanceGroup newItem) {
            boolean isSame = oldItem.getId() == newItem.getId();
            return  isSame;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProductInstanceGroup oldItem, @NonNull ProductInstanceGroup newItem) {
            boolean isSame = Objects.equals( oldItem, newItem );
            return  isSame;
        }
    }
}