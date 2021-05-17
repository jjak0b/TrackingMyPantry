package com.jjak0b.android.trackingmypantry.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Product;

import java.util.List;

public class RegisterProductViewModel extends ViewModel {

    private MutableLiveData<Product> product;

    private MutableLiveData<String> barcode;

    private PantryRepository pantryRepository;

    private LiveData<List<Product>> matchingProductsList;

    public RegisterProductViewModel() {
        pantryRepository = PantryRepository.getInstance();
        barcode = new MutableLiveData<>();
        product = new MutableLiveData<>();
        matchingProductsList = pantryRepository.getMatchingProducts();
    }

    public void setBarcode(String barcode) {
        this.barcode.setValue( barcode );
        pantryRepository.updateMatchingProducts(barcode);
    }

    public LiveData<String> getBarcode() { return barcode; }


    public LiveData<List<Product>> getProducts() {
        return matchingProductsList;
    }
}