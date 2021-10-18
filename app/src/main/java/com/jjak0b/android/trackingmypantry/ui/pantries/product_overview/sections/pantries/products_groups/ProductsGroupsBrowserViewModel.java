package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
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

    public ListenableFuture<Void> consume(ProductInstanceGroup entry, int amount){
        return pantryRepository.updateAndMergeProductInstanceGroup(entry);
    }
}