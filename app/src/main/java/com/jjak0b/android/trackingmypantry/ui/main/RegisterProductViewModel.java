package com.jjak0b.android.trackingmypantry.ui.main;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.model.Product;

import java.util.List;
import java.util.Objects;

import java9.util.concurrent.CompletableFuture;

public class RegisterProductViewModel extends AndroidViewModel {

    private MutableLiveData<String> barcode;

    private PantryRepository pantryRepository;

    private LiveData<List<Product>> matchingProductsList;

    private MutableLiveData<Product> product;

    private MutableLiveData<Product.Builder> productBuilder;

    public RegisterProductViewModel(Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        barcode = new MutableLiveData<>();
        product = new MutableLiveData<>();
        productBuilder = new MutableLiveData<>();
        matchingProductsList = pantryRepository.getMatchingProducts();
    }

    public void setBarcode(String barcode) {
        this.barcode.setValue( barcode );
        pantryRepository.updateMatchingProducts(barcode);
    }

    public LiveData<Product.Builder> getProductBuilder() {
         return productBuilder;
    }

    public LiveData<String> getBarcode() { return barcode; }


    public LiveData<List<Product>> getProducts() {
        return matchingProductsList;
    }

    public void setProduct(Product product) {

        Product.Builder productBuilder = new Product.Builder()
                .from( product );

        this.productBuilder.setValue( productBuilder );

        if( product != null && product.getId() != null ) {
            for (Product p : Objects.requireNonNull(matchingProductsList.getValue())) {
                if (product.getId().equals( p.getId() ) ) {
                    pantryRepository.voteProduct(p.getId(), 1);
                }
                // Note: API allow only 1 vote per product list
                // else {
                //      pantryRepository.voteProduct(p.getId(), -1);
                //}
            }
        }
    }

    public CompletableFuture<Void> registerProduct() {
        Product p = productBuilder.getValue()
                .build();



        return pantryRepository.addProduct(new Product.Builder()
                .from(p)
                .setProductId(null)
                .build()
        );
    }

    public LiveData<Product> getProduct() {
        return this.product;
    }
}