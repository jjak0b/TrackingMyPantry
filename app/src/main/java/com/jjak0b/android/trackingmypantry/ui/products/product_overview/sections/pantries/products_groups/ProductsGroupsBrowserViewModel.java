package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.repositories.PantriesRepository;
import com.jjak0b.android.trackingmypantry.ui.util.ItemSourceViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ProductsGroupsBrowserViewModel extends ItemSourceViewModel<List<ProductInstanceGroup>> {
    private PantriesRepository pantriesRepository;
    private LiveData<Resource<List<ProductInstanceGroup>>> mDefaultList;
    public ProductsGroupsBrowserViewModel(Application application) {
        super(application);
        pantriesRepository = PantriesRepository.getInstance(application);
        mDefaultList = new MutableLiveData<>(Resource.success(new ArrayList<>(0)));
    }

    @MainThread
    public void setGroupsOf(
            @NonNull LiveData<Resource<UserProduct>> mProductSource,
            @NonNull LiveData<Resource<Pantry>> mPantrySource
    ) {

        final LiveData<Resource<List<ProductInstanceGroup>>> source = Transformations.forward(mProductSource, productResource -> {
            return Transformations.forward(mPantrySource, pantryResource -> {
                if( productResource.getData() != null && pantryResource.getData() != null  ) {
                    return pantriesRepository.getContent(
                            productResource.getData().getBarcode(),
                            pantryResource.getData().getId()
                    );
                }
                else {
                    return mDefaultList;
                }
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


    public LiveData<Resource<Long>> moveToPantry(ProductInstanceGroup entry, Pantry destination, int quantity){
        if( quantity < 1 || entry.getPantryId() == destination.getId() ) {
            return new MutableLiveData<>(Resource.error(null, null));
        }

        return pantriesRepository.moveGroupToPantry(entry, destination, quantity);
    }

    public LiveData<Resource<Void>> delete( ProductInstanceGroup entry, int quantity){

        // remove the item on adapter
        if( entry.getQuantity() <= quantity){
            return pantriesRepository.deleteGroup(entry);
        }
        else {
            ProductInstanceGroup updatedGroup = ProductInstanceGroup.from(entry);
            updatedGroup.setQuantity( updatedGroup.getQuantity() - quantity );
            return androidx.lifecycle.Transformations.map(
                    pantriesRepository.updateGroup(updatedGroup),
                    voidMapFunc::apply
            );
        }
    }

    public LiveData<Resource<Void>> consume(ProductInstanceGroup entry, int amountPercent){

        ProductInstanceGroup updatedEntry = ProductInstanceGroup.from(entry);

        if( amountPercent <= 0 ){
            return new MutableLiveData<>(Resource.error(null, null));
        }

        // add the consumed entry as new entry and update quantity of old one
        if( entry.getQuantity() > 1 ) {

            ProductInstanceGroup consumedEntry = ProductInstanceGroup.from(entry);
            consumedEntry.setId(0);
            consumedEntry.setCurrentAmountPercent(updatedEntry.getCurrentAmountPercent()-amountPercent);
            consumedEntry.setQuantity(1);

            updatedEntry.setQuantity(updatedEntry.getQuantity()-consumedEntry.getQuantity());


            LiveData<Resource<Long>> onConsume = Transformations.forwardOnce( pantriesRepository.updateGroup(updatedEntry), input -> {
                return pantriesRepository.addGroup(consumedEntry, null, Pantry.creteDummy(updatedEntry.getPantryId()) );
            });

            return androidx.lifecycle.Transformations.map(onConsume, voidMapFunc::apply );
        }
        // we have only 1 entry so just update it
        else {
            updatedEntry.setCurrentAmountPercent(entry.getCurrentAmountPercent()-amountPercent);

            if( updatedEntry.getCurrentAmountPercent() > 0 ){
                return androidx.lifecycle.Transformations.map(
                        pantriesRepository.updateAndMergeGroups(updatedEntry),
                        voidMapFunc::apply
                );
            }
            else {
                return androidx.lifecycle.Transformations.map(
                        pantriesRepository.deleteGroup(updatedEntry),
                        voidMapFunc::apply
                );
            }
        }
    }

    private final Function<Resource<?>, Resource<Void>> voidMapFunc = input -> {
        switch (input.getStatus()) {
            case SUCCESS:
                return Resource.success(null);
            case ERROR:
                return Resource.error(input.getError(), null);
            default:
                return Resource.loading(null);
        }
    };
}