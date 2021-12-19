package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInfoViewModel;

public class NewProductFormViewModel extends ProductInfoViewModel {

    public ProductsRepository repository;

    public NewProductFormViewModel(Application application) {
        super(application);
        repository = ProductsRepository.getInstance(application);
    }

    public LiveData<Resource<Product>> submit(Product product) {
        return repository.register(product);
    }
}
