package com.jjak0b.android.trackingmypantry.ui.products.product_overview;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.repositories.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;

public class ProductOverviewViewModel extends AndroidViewModel {

    private ProductsRepository productsRepository;
    private PantryRepository pantryRepository;
    private MutableLiveData<String> productID;
    private MutableLiveData<ProductWithTags> mProduct;

    public ProductOverviewViewModel(@NonNull Application application) {
        super(application);
        productsRepository = ProductsRepository.getInstance(application);
        pantryRepository = PantryRepository.getInstance(application);
        productID = new MutableLiveData<>();

        mProduct = (MutableLiveData<ProductWithTags>)Transformations.switchMap(
                productID,
                id -> pantryRepository.getProductWithTags(id));
    }

    public LiveData<Resource<Product>> get(String barcode) {
        return productsRepository.get(barcode);
    }

    public void setProductID( String id ){
        productID.setValue( id );
    }

    public LiveData<ProductWithTags> getProduct() {
        return mProduct;
    }

    public void setProduct( ProductWithTags product ) {
        mProduct.setValue(product);
    }
}