package com.jjak0b.android.trackingmypantry.ui.register_product.suggestions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;

import java.util.List;

public class SuggestedProductsViewModel extends AndroidViewModel {

    private ProductsRepository repository;

    public SuggestedProductsViewModel(@NonNull Application application) {
        super(application);
        repository = ProductsRepository.getInstance(application);
    }

    public LiveData<Resource<List<Product>>> getProducts(String barcode) {
        return repository.search(barcode);
    }

    public LiveData<Resource<Product>> vote(@NonNull Product product) {
        return repository.register(product);
    }
}
