package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;

import java.util.List;

public class ProductsGroupsBrowserViewModel extends AndroidViewModel {
    private PantryRepository pantryRepository;
    private MutableLiveData<List<ProductInstanceGroup>> groups;

    public ProductsGroupsBrowserViewModel(Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        groups = new MutableLiveData<>(null);
    }

    public void setGroups(List<ProductInstanceGroup> groups){
        this.groups.postValue(groups);
    }

    public LiveData<List<ProductInstanceGroup>> getGroups() {
        return groups;
    }

    @Override
    protected void onCleared() {
        this.groups.setValue(null);
        this.groups = null;
        super.onCleared();
    }

    public ListenableFuture<Void> deleteProductInstanceGroup(ProductInstanceGroup... entry){
        return pantryRepository.deleteProductInstanceGroup(entry);
    }

    public ListenableFuture<Void> moveProductInstanceGroupToPantry(ProductInstanceGroup entry, Pantry destination, int quantity){
        return pantryRepository.moveProductInstanceGroupToPantry(entry, destination, quantity);
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