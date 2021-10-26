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
    private MutableLiveData<Pantry> pantry;

    public ProductsGroupsBrowserViewModel(Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        groups = new MutableLiveData<>(null);
        pantry = new MutableLiveData<>(null);
    }

    public void setGroups(List<ProductInstanceGroup> groups){
        if( groups == null ){
            this.groups.postValue(null);
        }
        else if( groups instanceof ArrayList ){
            this.groups.postValue(groups);
        }
        else {
            this.groups.postValue(new ArrayList<>(groups));
        }
    }

    public LiveData<List<ProductInstanceGroup>> getGroups() {
        return groups;
    }

    public LiveData<List<Pantry>> getAvailablePantries() {
        return pantryRepository.getPantries();
    }

    public LiveData<Pantry> getPantry() {
        return pantry;
    }

    public void setPantry(Pantry pantry) {
        this.pantry.postValue(pantry);
    }

    @Override
    protected void onCleared() {
        this.groups.setValue(null);
        this.groups = null;
        this.pantry.setValue(null);
        this.pantry = null;
        super.onCleared();
    }

    public ListenableFuture<Void> deleteProductInstanceGroup(ProductInstanceGroup... entry){
        return pantryRepository.deleteProductInstanceGroup(entry);
    }

    public ListenableFuture<Void> moveToPantry(ProductInstanceGroup entry, Pantry destination, int quantity){
        if( quantity < 1 || entry.getPantryId() == destination.getId() ) {
            return Futures.immediateFuture(null);
        }

        return pantryRepository.moveProductInstanceGroupToPantry(entry, destination, quantity);
    }

    public ListenableFuture<Void> delete( ProductInstanceGroup entry, int quantity){

        // remove the item on adapter
        if( entry.getQuantity() <= quantity){
            return pantryRepository.deleteProductInstanceGroup(entry);
        }
        else {
            ProductInstanceGroup updatedGroup = ProductInstanceGroup.from(entry);
            updatedGroup.setQuantity( updatedGroup.getQuantity() - quantity );
            return pantryRepository.updateProductInstanceGroup(updatedGroup);
        }
    }

    public ListenableFuture<Void> consume(ProductInstanceGroup entry, int amountPercent){

        ProductInstanceGroup updatedEntry = ProductInstanceGroup.from(entry);

        if( amountPercent <= 0 ){
            return Futures.immediateFuture(null);
        }

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