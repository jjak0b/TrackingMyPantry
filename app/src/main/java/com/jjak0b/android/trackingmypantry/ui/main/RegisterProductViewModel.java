package com.jjak0b.android.trackingmypantry.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Product;

import java.util.List;
import java.util.Objects;

public class RegisterProductViewModel extends ViewModel {

    private MutableLiveData<String> barcode;

    private PantryRepository pantryRepository;

    private LiveData<List<Product>> matchingProductsList;

    private MutableLiveData<Product> product;

    private MutableLiveData<Product.Builder> productBuilder;

    public RegisterProductViewModel() {
        pantryRepository = PantryRepository.getInstance();
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
                } else {
                    pantryRepository.voteProduct(p.getId(), -1);
                }
            }
        }
    }

    public LiveData<Product> getProduct() {
        return this.product;
    }
}