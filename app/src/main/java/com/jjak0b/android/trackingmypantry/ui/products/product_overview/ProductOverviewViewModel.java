package com.jjak0b.android.trackingmypantry.ui.products.product_overview;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;

public class ProductOverviewViewModel extends AndroidViewModel {

    private ProductsRepository productsRepository;

    public ProductOverviewViewModel(@NonNull Application application) {
        super(application);
        productsRepository = ProductsRepository.getInstance(application);
    }

    public LiveData<Resource<UserProduct>> get(String barcode) {
        return productsRepository.get(barcode);
    }

    public LiveData<Resource<UserProduct>> remove(UserProduct product) {
        return productsRepository.remove(product);
    }
}