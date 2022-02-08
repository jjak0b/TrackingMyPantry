package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.purchase_locations;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.repositories.PurchasesRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PurchaseLocationsViewModel extends AndroidViewModel {

    private PurchasesRepository purchasesRepository;
    private MutableLiveData<Resource<UserProduct>> product;
    private LiveData<Resource<Map<Place, List<PurchaseInfo>>>> purchaseInfoList;

    public PurchaseLocationsViewModel(Application application) {
        super(application);
        purchasesRepository = PurchasesRepository.getInstance(application);

        product = new MutableLiveData<>(Resource.loading(null));
        purchaseInfoList = Transformations.forward(product, resource -> {
            UserProduct product = resource.getData();
            return purchasesRepository.getAllPurchasePlacesOf(product.getBarcode());
        });
    }


    public void setProduct(Resource<UserProduct> product) {
        if(!Objects.equals(product, this.product.getValue()))
            this.product.setValue(product);
    }

    public LiveData<Resource<Map<Place, List<PurchaseInfo>>>> getPurchaseInfoList() {
        return purchaseInfoList;
    }
}