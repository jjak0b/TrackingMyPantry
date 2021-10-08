package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups.model;

import android.view.View;

import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;

import java.util.List;

public interface ProductInstanceGroupInteractionsListener {
    void onItemClicked(int pantryPosition, View pantryView, Pantry item, List<ProductInstanceGroup> content);
    void onItemLongClicked(int pantryPosition, View pantryView, Pantry item, List<ProductInstanceGroup> content);
}
