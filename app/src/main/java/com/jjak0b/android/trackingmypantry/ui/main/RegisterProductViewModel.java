package com.jjak0b.android.trackingmypantry.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Product;

import java.util.List;

public class RegisterProductViewModel extends ViewModel {

    private MutableLiveData<Product> product = new MutableLiveData<>();

    private MutableLiveData<String> barcode = new MutableLiveData<>();

    private PantryRepository pantryRepository;

    RegisterProductViewModel() {
        pantryRepository = PantryRepository.getInstance();
    }

    public void setBarcode(String barcode) {
        this.barcode.setValue( barcode );
    }

    public LiveData<String> getBarcode() { return barcode; }

    public LiveData<List<Product>> getProducts() {
        return pantryRepository.getProducts(getBarcode().getValue());
    }
}