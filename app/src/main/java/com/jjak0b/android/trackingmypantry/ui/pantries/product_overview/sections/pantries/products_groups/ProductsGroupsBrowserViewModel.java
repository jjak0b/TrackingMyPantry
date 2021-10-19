package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

public class ProductsGroupsBrowserViewModel extends AndroidViewModel {
    private PantryRepository pantryRepository;
    private MutableLiveData<List<ProductInstanceGroup>> groups;
    // for deletion we use integer entry value as entry updated value
    // and integer value as quantity removed
    private MutableLiveData<LinkedList<PendingDeletionEntry<ProductInstanceGroup, Integer>>> pendingDeletions;

    public ProductsGroupsBrowserViewModel(Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        groups = new MutableLiveData<>(null);
        pendingDeletions = new MutableLiveData<>(new LinkedList<>());
    }

    public void setGroups(List<ProductInstanceGroup> groups){
        if( groups instanceof ArrayList ){
            this.groups.postValue(groups);
        }
        else {
            this.groups.postValue(new ArrayList<>(groups));
        }
    }

    public LiveData<List<ProductInstanceGroup>> getGroups() {
        return groups;
    }

    @Override
    protected void onCleared() {
        this.groups.setValue(null);
        this.groups = null;
        // this.clearDeletionQueue();
        this.pendingDeletions.setValue(null);
        this.pendingDeletions = null;
        super.onCleared();
    }

    public ListenableFuture<Void> deleteProductInstanceGroup(ProductInstanceGroup... entry){
        return pantryRepository.deleteProductInstanceGroup(entry);
    }

    public ListenableFuture<Void> moveProductInstanceGroupToPantry(ProductInstanceGroup entry, Pantry destination, int quantity){
        return pantryRepository.moveProductInstanceGroupToPantry(entry, destination, quantity);
    }

    public void delete( ProductInstanceGroup entry, int position, int quantity){


        // remove the item on adapter
        if( entry.getQuantity() <= quantity){
            groups.getValue()
                    .remove(position);
            pendingDeletions.getValue()
                    .addLast(new PendingDeletionEntry<>(entry, null, PendingDeletionEntry.IS_DELETE));
        }
        else {
            ProductInstanceGroup removedGroup = ProductInstanceGroup.from(entry);
            removedGroup.setQuantity(quantity);

            ProductInstanceGroup updatedGroup = groups.getValue()
                    .get(position);
            updatedGroup.setQuantity( updatedGroup.getQuantity() - quantity );

            pendingDeletions.getValue()
                    .addLast(new PendingDeletionEntry<>(updatedGroup, quantity, PendingDeletionEntry.IS_UPDATE));
        }

        setGroups(groups.getValue()); // update view
    }

    public void undoLastDeletionAtIndex(int position) {
        ProductInstanceGroup groupAtIndex = groups.getValue().get(position);
        PendingDeletionEntry<ProductInstanceGroup, Integer> lastRemovedPendingEntry = pendingDeletions.getValue().removeLast();
        ProductInstanceGroup lastRemoved =  lastRemovedPendingEntry.getEntry();

        if(lastRemovedPendingEntry.is(PendingDeletionEntry.IS_DELETE)) {
            groups.getValue().add(position, lastRemoved);
        }
        else if( groupAtIndex != null ){
            // if we split it on deletion, then re-merge it
            groupAtIndex.setQuantity(groupAtIndex.getQuantity() + lastRemovedPendingEntry .getMetadata());
        }

        // update
        setGroups(groups.getValue()); // update view
    }

    void completeDeletions() {

        Stack<ProductInstanceGroup> listToDelete = new Stack<>();
        Stack<ProductInstanceGroup> listToUpdate= new Stack<>();

        for ( PendingDeletionEntry<ProductInstanceGroup, Integer> pendingDeletionEntry : pendingDeletions.getValue() ) {
            if(pendingDeletionEntry.is(PendingDeletionEntry.IS_DELETE)){
                listToDelete.add(pendingDeletionEntry.getEntry());
            }
            else if( pendingDeletionEntry.is(PendingDeletionEntry.IS_UPDATE)){
                listToUpdate.add(pendingDeletionEntry.getEntry());
            }
        }

        Object[] from2Del = listToDelete.toArray();
        ProductInstanceGroup[] toDel = new ProductInstanceGroup[from2Del.length];
        System.arraycopy(from2Del, 0, toDel, 0, from2Del.length);

        Object[] from2Up = listToUpdate.toArray();
        ProductInstanceGroup[] toUpd = new ProductInstanceGroup[from2Up.length];
        System.arraycopy(from2Up, 0, toUpd, 0, from2Up.length);

        Futures.allAsList(
                pantryRepository.updateProductInstanceGroup(toUpd),
                pantryRepository.deleteProductInstanceGroup(toDel)
        );

        pendingDeletions.getValue().clear();
        pendingDeletions.setValue(null);
    }

    public ListenableFuture<Void> consume(ProductInstanceGroup entry, int amountPercent){

        ProductInstanceGroup updatedEntry = ProductInstanceGroup.from(entry);

        // add the consumed entry as new entry and update quantity of old one
        if( entry.getQuantity() > 1 ) {

            ProductInstanceGroup consumedEntry = ProductInstanceGroup.from(entry);
            consumedEntry.setId(0);
            consumedEntry.setCurrentAmountPercent(updatedEntry.getCurrentAmountPercent()-amountPercent);
            consumedEntry.setQuantity(1);

            updatedEntry.setQuantity(updatedEntry.getQuantity()-consumedEntry.getQuantity());

            return Futures.transform(
                    Futures.allAsList(
                            pantryRepository.updateProductInstanceGroup(updatedEntry),
                            pantryRepository.addProductInstanceGroup(
                                    consumedEntry,
                                    null,
                                    Pantry.creteDummy(updatedEntry.getPantryId())
                            )
                    ),
                    input -> null,
                    MoreExecutors.directExecutor()
            );
        }
        // we have only 1 entry so just update it
        else {
            updatedEntry.setCurrentAmountPercent(entry.getCurrentAmountPercent()-amountPercent);

            if( updatedEntry.getCurrentAmountPercent() > 0 ){
                return pantryRepository.updateAndMergeProductInstanceGroup(updatedEntry);
            }
            else {
                return pantryRepository.deleteProductInstanceGroup(updatedEntry);
            }
        }
    }
}

class PendingDeletionEntry<T, MD> {
    public static final int IS_UPDATE = 0;
    public static final int IS_DELETE = 1;

    private int type;
    private T entry;
    private MD metadata;

    public PendingDeletionEntry( T entry, MD metadata, int type ){
        this.entry = entry;
        this.type = type;
        this.metadata = metadata;
    }

    public boolean is( int type ){
        return this.type == type;
    }

    public T getEntry() {
        return entry;
    }

    public MD getMetadata() {
        return metadata;
    }
}