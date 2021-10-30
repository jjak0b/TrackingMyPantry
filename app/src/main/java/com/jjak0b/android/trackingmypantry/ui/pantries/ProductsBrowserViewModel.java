package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.app.Application;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.jjak0b.android.trackingmypantry.data.FilterState;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.SearchFilterState;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.ui.SearchFilterViewModel;

import java.util.List;

public class ProductsBrowserViewModel extends AndroidViewModel {

    private PantryRepository mPantryRepository;

    private LiveData<List<ProductWithTags>> mProductsList;
    private MutableLiveData<FilterState> mFilterState;

    public ProductsBrowserViewModel(Application application ) {
        super(application);
        mPantryRepository = PantryRepository.getInstance(application);
        mFilterState = new MutableLiveData<>( null );
        // https://stackoverflow.com/questions/48769812/best-practice-runtime-filters-with-room-and-livedata
        mProductsList = Transformations.switchMap(
                mFilterState,
                new Function<FilterState, LiveData<List<ProductWithTags>>>() {
                    @Override
                    public LiveData<List<ProductWithTags>> apply(FilterState filter) {
                        return mPantryRepository.getProductsWithTags(filter);
                    }
                }
        );
    }


    public LiveData<List<ProductWithTags>> getProductsWithTags(){
        return mProductsList;
    }

    public void setFilterState( SearchFilterState state ){

        boolean isNotValid = state == null || state.query == null || state.query.length() < 1;
        if( isNotValid ){
            mFilterState.setValue( null );
            return;
        }

        FilterState filter = new FilterState();
        filter.name = state.query;
        filter.description = state.query;
        filter.barcode = state.query;

        mFilterState.postValue(filter);
    }

     /*
    private void updateList(){

       // using mProductsList as MediatorLiveData
        if( mCurrentProductList != null ){
            mProductsList.removeSource(mCurrentProductList);
            mCurrentProductList = null;
        }

        mCurrentProductList = mPantryRepository.getProducts();
        mProductsList.addSource(mCurrentProductList, products -> mProductsList.setValue( products ) );
    }
    */

}