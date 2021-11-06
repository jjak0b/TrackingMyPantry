package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.ui.ItemViewModel;
import com.jjak0b.android.trackingmypantry.data.db.relationships.PantryWithProductInstanceGroups;

public class PantryViewModel extends ViewModel implements ItemViewModel<PantryWithProductInstanceGroups> {

    private PantryWithProductInstanceGroups item;
    private MutableLiveData<Boolean> isExpanded;
    private LiveEvent<Boolean> onExpand;
    private PantryInteractionsListener listener;

    public PantryViewModel() {
        isExpanded = new MutableLiveData<>(false);
        onExpand = new LiveEvent<>();
    }

    public void expand() {
        onExpand.setValue(true);
        isExpanded.setValue(true);
    }

    public void collapse(){
        onExpand.setValue(false);
        isExpanded.setValue(false);
    }

    /**
     * Notify the expand if collapsed and viceversa
     * @return the old expanded state
     */
    public boolean toggle() {
        boolean isExpanded = this.isExpanded.getValue();
        if(isExpanded){
            collapse();
        }
        else {
            expand();
        }
        return isExpanded;
    }

    @Override
    public PantryWithProductInstanceGroups getItem() {
        return item;
    }

    @Override
    public void setItem(PantryWithProductInstanceGroups item) {
        this.item = item;
    }

    public LiveData<Boolean> getExpanded() {
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
