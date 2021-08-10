package com.jjak0b.android.trackingmypantry.ui.pantries;

import androidx.lifecycle.ViewModel;

import com.jjak0b.android.trackingmypantry.ItemViewModel;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;

public class PantryViewModel extends ViewModel implements ItemViewModel<PantryWithProductInstanceGroups> {

    private PantryWithProductInstanceGroups item;
    private boolean isExpanded;
    private PantryInteractionsListener listener;
    @Override
    public PantryWithProductInstanceGroups getItem() {
        return item;
    }

    @Override
    public void setItem(PantryWithProductInstanceGroups item) {
        this.item = item;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public boolean getExpanded() {
        return isExpanded;
    }

    public PantryInteractionsListener getInteractionsListener() {
        return listener;
    }

    public void setInteractionsListener( PantryInteractionsListener pantryInteractionsListener ) {
        this.listener = pantryInteractionsListener;
    }

    @Override
    protected void onCleared() {
        item = null;
        listener = null;
        super.onCleared();
    }
}
