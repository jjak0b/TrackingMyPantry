package com.jjak0b.android.trackingmypantry.ui.register_product;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;

public class SharedProductViewModel extends ViewModel {

    private MediatorLiveData<Resource<Product>> mProduct;
    private LiveData<Resource<Product>> mProductSource;

    public SharedProductViewModel() {
        super();
        mProduct = new MediatorLiveData<>();
        setProductSource(null);
    }

    public LiveData<Resource<Product>> getProduct() {
        return mProduct;
    }

    @MainThread
    public void setProductSource( LiveData<Resource<Product>> mSource ) {
        if( mSource != null ) {
            if( mProductSource != null ) {
                mProduct.removeSource(mProductSource);
            }
            mProductSource = mSource;
            mProduct.addSource(mSource, mProduct::setValue );
        }
        else {
            setProductSource(new MutableLiveData<>(Resource.loading(null)));
        }
    }
}
