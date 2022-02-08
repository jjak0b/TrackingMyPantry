package com.jjak0b.android.trackingmypantry.ui.products;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;
import com.jjak0b.android.trackingmypantry.ui.util.MultiSelectItemsViewModel;

import java.util.List;

public class MultiSelectProductsTagsViewModel extends MultiSelectItemsViewModel<ProductTag> {

    private ProductsRepository productsRepository;
    private LiveData<Resource<List<ProductTag>>> mSearchTagsSuggestions;

    public MultiSelectProductsTagsViewModel(Application application) {
        super(application);
        productsRepository = ProductsRepository.getInstance(application);
        mSearchTagsSuggestions = productsRepository.getTags();
    }

    @Override
    public LiveData<Resource<List<ProductTag>>> getAllItems() {
        return mSearchTagsSuggestions;
    }
}