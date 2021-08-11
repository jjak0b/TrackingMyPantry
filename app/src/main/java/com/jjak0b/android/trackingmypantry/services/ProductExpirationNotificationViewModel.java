package com.jjak0b.android.trackingmypantry.services;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductInstanceGroupInfo;

import java.util.List;

public class ProductExpirationNotificationViewModel extends AndroidViewModel {
    PantryRepository pantryRepository;
    LiveData<List<ProductInstanceGroupInfo>> list;

    public ProductExpirationNotificationViewModel(@NonNull Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        list = pantryRepository.getLiveInfoOfAll(null, null);
    }

    public LiveData<List<ProductInstanceGroupInfo>> getAllProductInfo() {
        return list;
    }
}
