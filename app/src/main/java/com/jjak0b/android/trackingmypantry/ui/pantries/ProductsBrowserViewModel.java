package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.app.Application;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.jjak0b.android.trackingmypantry.data.FilterState;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Product;

import java.util.List;

public class ProductsBrowserViewModel extends AndroidViewModel {

    private PantryRepository mPantryRepository;

    private LiveData<List<Product>> mProductsList;
    private MutableLiveData<FilterState> mFilterState;

    public ProductsBrowserViewModel(Application application ) {
        super(application);
        mPantryRepository = PantryRepository.getInstance(application);
        mFilterState = new MutableLiveData<>( new FilterState() );
        // https://stackoverflow.com/questions/48769812/best-practice-runtime-filters-with-room-and-livedata
        mProductsList = Transformations.switchMap(
                mFilterState,
                new Function<FilterState, LiveData<List<Product>>>() {
                    @Override
                    public LiveData<List<Product>> apply(FilterState input) {
                        return mPantryRepository.getProducts();
                    }
                }
        );
    }


    public LiveData<List<Product>> getProducts(){
        return mProductsList;
    }

    public void setFilterState( FilterState state ){
        mFilterState.setValue( state );
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