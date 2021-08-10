package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.view.View;

import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.ProductInstanceGroupTableViewAdapter;

public interface PantryInteractionsListener {
    void onItemClicked(int pantryPosition, View pantryView, int pantryItemPosition, View pantryItemView, ProductInstanceGroupTableViewAdapter pantryItemsAdapter );
    void onItemLongClicked(int pantryPosition, View pantryView, int pantryItemPosition, View pantryItemView, ProductInstanceGroupTableViewAdapter pantryItemsAdapter );
}
