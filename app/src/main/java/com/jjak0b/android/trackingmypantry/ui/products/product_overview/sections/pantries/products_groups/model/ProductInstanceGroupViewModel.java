package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups.model;

import androidx.lifecycle.ViewModel;

import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.ItemViewModel;

public class ProductInstanceGroupViewModel extends ViewModel implements ItemViewModel<ProductInstanceGroup> {

    private ProductInstanceGroup item;
    private ProductInstanceGroupInteractionsListener listener;

    public ProductInstanceGroupViewModel() {

    }

    @Override
    public ProductInstanceGroup getItem() {
        return item;
    }

    @Override
    public void setItem(ProductInstanceGroup item) {
        this.item = item;
    }

    public ProductInstanceGroupInteractionsListener getInteractionsListener() {
        return listener;
    }

    public void setInteractionsListener( ProductInstanceGroupInteractionsListener interactionsListener ) {
        this.listener = interactionsListener;
    }

    @Override
    protected void onCleared() {
        item = null;
        listener = null;
        super.onCleared();
    }
}
