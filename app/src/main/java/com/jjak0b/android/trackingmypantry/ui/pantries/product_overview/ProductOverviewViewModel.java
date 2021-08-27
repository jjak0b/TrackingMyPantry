package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;

import java.util.List;

public class ProductOverviewViewModel extends AndroidViewModel {

    private PantryRepository pantryRepository;
    private MutableLiveData<String> productID;
    private LiveData<List<PantryWithProductInstanceGroups>> list;
    private LiveData<ProductWithTags> mProduct;

    public ProductOverviewViewModel(@NonNull Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        productID = new MutableLiveData<>();

        mProduct = Transformations.switchMap(
                productID,
                id -> pantryRepository.getProductWithTags(id));
    }

    public void setProductID( String id ){
        productID.setValue( id );
    }

    public LiveData<ProductWithTags> getProduct() {
        return mProduct;
    }
}