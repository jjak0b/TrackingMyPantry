package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.purchase_locations;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.relationships.PlaceWithPurchases;
import com.jjak0b.android.trackingmypantry.data.repositories.PurchasesRepository;

import java.util.List;
import java.util.Objects;

public class PurchaseLocationsViewModel extends AndroidViewModel {

    private PurchasesRepository purchasesRepository;
    private MutableLiveData<Resource<Product>> product;
    private LiveData<Resource<List<PlaceWithPurchases>>> purchaseInfoList;

    public PurchaseLocationsViewModel(Application application) {
        super(application);
        purchasesRepository = PurchasesRepository.getInstance(application);

        product = new MutableLiveData<>(Resource.loading(null));
        purchaseInfoList = Transformations.forward(product, resource -> {
            Product product = resource.getData();
            return purchasesRepository.getAllPurchasePlacesOf(product.getBarcode());
        });
    }


    public void setProduct(Resource<Product> product) {
        if(!Objects.equals(product, this.product.getValue()))
            this.product.setValue(product);
    }

    public LiveData<Resource<List<PlaceWithPurchases>>> getPurchaseInfoList() {
        return purchaseInfoList;
    }
}