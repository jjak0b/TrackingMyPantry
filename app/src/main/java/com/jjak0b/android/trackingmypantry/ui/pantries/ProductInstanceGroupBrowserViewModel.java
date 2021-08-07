package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;

import java.util.List;

public class ProductInstanceGroupBrowserViewModel extends AndroidViewModel {
    private PantryRepository pantryRepository;
    private LiveData<List<ProductInstanceGroup>> mItems;
    private MutableLiveData<Filter> mFilter;

    public ProductInstanceGroupBrowserViewModel(@NonNull Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        mFilter = new MutableLiveData<>();
        mItems = Transformations.switchMap(
                mFilter,
                new Function<Filter, LiveData<List<ProductInstanceGroup>>>() {
                    @Override
                    public LiveData<List<ProductInstanceGroup>> apply(Filter input) {
                        return pantryRepository.getProductInstanceGroupsOf(input.pantryID, input.productID);
                    }
                });
    }

    public ListenableFuture<Void> deleteProductInstanceGroup(ProductInstanceGroup entry){
        return pantryRepository.deleteProductInstanceGroup(entry);
    }

    public ListenableFuture<Void> moveProductInstanceGroupToPantry(ProductInstanceGroup entry, Pantry destination ){
        return pantryRepository.moveProductInstanceGroupToPantry(entry, destination);
    }

    LiveData<List<Pantry>> getPantries() {
        return pantryRepository.getPantries();
    }


    public LiveData<List<ProductInstanceGroup>> getItems() {
        return mItems;
    }

    public void setDataParameters(String productID, long pantryID){
        Filter args = new Filter();
        args.pantryID = pantryID;
        args.productID = productID;
        mFilter.setValue( args );
    }

    private class Filter {
        String productID;
        long pantryID;
    }
}