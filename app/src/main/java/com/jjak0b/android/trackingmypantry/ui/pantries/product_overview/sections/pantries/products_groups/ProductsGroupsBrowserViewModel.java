package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;

import java.util.List;

public class ProductsGroupsBrowserViewModel extends ViewModel {
    private MutableLiveData<List<ProductInstanceGroup>> groups;

    public ProductsGroupsBrowserViewModel() {
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
}