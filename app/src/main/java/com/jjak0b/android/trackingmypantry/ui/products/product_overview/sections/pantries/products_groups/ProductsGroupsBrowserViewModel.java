package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.repositories.PantriesRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.PantryRepository;
import com.jjak0b.android.trackingmypantry.ui.util.ItemSourceViewModel;

import java.util.List;

public class ProductsGroupsBrowserViewModel extends ItemSourceViewModel<List<ProductInstanceGroup>> {
    private PantryRepository pantryRepository;
    private PantriesRepository pantriesRepository;

    public ProductsGroupsBrowserViewModel(Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        pantriesRepository = PantriesRepository.getInstance(application);
    }

    @MainThread
    public void setGroupsOf(
            @NonNull LiveData<Resource<Product>> mProductSource,
            @NonNull LiveData<Resource<Pantry>> mPantrySource
    ) {

        final LiveData<Resource<List<ProductInstanceGroup>>> source = Transformations.forward(mProductSource, productResource -> {
            return Transformations.forward(mPantrySource, pantryResource -> {
                return pantriesRepository.getContent(
                        productResource.getData().getBarcode(),
                        pantryResource.getData().getId()
                );
            });
        });

        setItemSource(source);
    }

    public LiveData<Resource<List<Pantry>>> getAvailablePantries() {
        return pantriesRepository.getPantries();
    }

    @Override
    protected void onCleared() {
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