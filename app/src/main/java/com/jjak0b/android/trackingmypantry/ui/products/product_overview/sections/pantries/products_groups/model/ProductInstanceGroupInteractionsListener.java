package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups.model;

import android.view.View;

import androidx.appcompat.widget.PopupMenu;

import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;

import java.util.List;

public interface ProductInstanceGroupInteractionsListener {
    void onItemClicked(int pantryPosition, View pantryView, Pantry item, List<ProductInstanceGroup> content);
    void onItemLongClicked(int pantryPosition, View pantryView, Pantry item, List<ProductInstanceGroup> content);
    void onConsume( int groupPosition, ProductInstanceGroup group, int amount);
    void onRemove( int groupPosition, ProductInstanceGroup group, int quantity);
    void onMove( int groupPosition, ProductInstanceGroup group);
    void onMore(int groupPosition, ProductInstanceGroup group, PopupMenu popupMenu);
}
