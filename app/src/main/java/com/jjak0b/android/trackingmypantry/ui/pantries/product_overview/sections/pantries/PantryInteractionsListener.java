package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries;

import android.view.View;

import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;

public interface PantryInteractionsListener {
    void onItemClicked(int pantryPosition, View pantryView, PantryWithProductInstanceGroups item);
    void onItemLongClicked(int pantryPosition, View pantryView, PantryWithProductInstanceGroups item);
}
