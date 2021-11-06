package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.purchase_locations;

import android.app.Application;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.jjak0b.android.trackingmypantry.data.repositories.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.relationships.PlaceWithPurchases;

import java.util.List;
import java.util.Objects;

public class PurchaseLocationsViewModel extends AndroidViewModel {

    private PantryRepository pantryRepository;
    private MutableLiveData<Product> product;
    private LiveData<List<PlaceWithPurchases>> purchaseInfoList;

    public PurchaseLocationsViewModel(Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);

        product = new MutableLiveData<>(null);
        purchaseInfoList = Transformations.switchMap(
                product,
                new Function<Product, LiveData<List<PlaceWithPurchases>>>() {
                    @Override
                    public LiveData<List<PlaceWithPurchases>> apply(Product input) {
                        if( product != null )
                            return pantryRepository.getAllPurchaseInfo(input.getId());
                        else
                            return new MutableLiveData<>(null);
                    }
                }
        );
    }


    public void setProduct(Product product) {
        if(!Objects.equals(product, this.product.getValue()))
            this.product.setValue(product);
    }

    public LiveData<List<PlaceWithPurchases>> getPurchaseInfoList() {
        return purchaseInfoList;
    }
}