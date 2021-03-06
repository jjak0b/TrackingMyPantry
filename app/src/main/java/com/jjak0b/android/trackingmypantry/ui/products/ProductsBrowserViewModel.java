package com.jjak0b.android.trackingmypantry.ui.products;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.filters.ProductFilterState;
import com.jjak0b.android.trackingmypantry.data.filters.SearchFilterState;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;

import java.util.List;

public class ProductsBrowserViewModel extends AndroidViewModel {

    private ProductsRepository productsRepository;
    private LiveData<Resource<List<ProductWithTags>>> mProductsList;
    private MutableLiveData<ProductFilterState> mFilterState;

    public ProductsBrowserViewModel(Application application ) {
        super(application);
        productsRepository = ProductsRepository.getInstance(application);
        mFilterState = new MutableLiveData<>( null );
        // https://stackoverflow.com/questions/48769812/best-practice-runtime-filters-with-room-and-livedata
        mProductsList = Transformations.switchMap(
                mFilterState,
                filter -> productsRepository.getDetails(filter)
        );
    }

    public LiveData<Resource<List<ProductWithTags>>> getProductsWithTags(){
        return mProductsList;
    }

    public void setFilterState( SearchFilterState state ){

        boolean isValid = state != null && (
                (state.query != null && state.query.length() > 0) ||
                (state.searchTags != null && !state.searchTags.isEmpty())
        );
        if( !isValid ){
            mFilterState.setValue( null );
            return;
        }

        ProductFilterState filter = new ProductFilterState();
        filter.name = state.query;
        filter.description = state.query;
        filter.barcode = state.query;
        filter.tagsIDs = state.searchTags;

        mFilterState.postValue(filter);
    }

}