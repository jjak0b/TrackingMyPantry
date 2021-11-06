package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries;

import android.view.View;

import com.jjak0b.android.trackingmypantry.data.db.relationships.PantryWithProductInstanceGroups;

public interface PantryInteractionsListener {
    void onItemClicked(int pantryPosition, View pantryView, PantryWithProductInstanceGroups item);
    void onItemLongClicked(int pantryPosition, View pantryView, PantryWithProductInstanceGroups item);
}
