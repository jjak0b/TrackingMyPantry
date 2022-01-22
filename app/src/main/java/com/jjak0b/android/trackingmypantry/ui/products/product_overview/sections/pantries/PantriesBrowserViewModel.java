package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.results.PantryDetails;
import com.jjak0b.android.trackingmypantry.data.repositories.PantriesRepository;

import java.util.List;
import java.util.Objects;

public class PantriesBrowserViewModel extends AndroidViewModel {

    private PantriesRepository pantriesRepository;

    private MutableLiveData<Resource<Product>> mProduct;
    private LiveData<Resource<List<PantryDetails>>> list;
    private MutableLiveData<Resource<Pantry>> mCurrentPantry;

    public PantriesBrowserViewModel(@NonNull Application application) {
        super(application);
        pantriesRepository = PantriesRepository.getInstance(application);

        mProduct = new MutableLiveData<>(Resource.loading(null));
        mCurrentPantry = new MutableLiveData<>(Resource.loading(null));
        list = Transformations.forward(mProduct, input -> {
            return pantriesRepository.getAllContaining(input.getData().getBarcode());
        });

        setProduct(Resource.loading(null));
        setCurrentPantry(null);
    }

    public LiveData<Resource<Pantry>> getCurrentPantry() {
        return mCurrentPantry;
    }

    public void setCurrentPantry(Pantry mCurrentPantry) {
        if( !Objects.equals(mCurrentPantry, this.mCurrentPantry.getValue().getData())) {
            this.mCurrentPantry.postValue(Resource.loading(mCurrentPantry));
            if(mCurrentPantry != null){
                this.mCurrentPantry.postValue(Resource.success(mCurrentPantry));
            }
        }
    }

    public void setProduct(Resource<Product> product ){
        if( !Objects.equals(product, mProduct.getValue()) )
            mProduct.setValue( product );
    }

    public LiveData<Resource<List<PantryDetails>>> getList(){
        return list;
    }

}