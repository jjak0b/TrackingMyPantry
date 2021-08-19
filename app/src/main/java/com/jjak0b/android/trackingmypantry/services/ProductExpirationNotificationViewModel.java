package com.jjak0b.android.trackingmypantry.services;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.data.LoginRepository;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductInstanceGroupInfo;

import java.util.List;

public class ProductExpirationNotificationViewModel extends AndroidViewModel {
    PantryRepository pantryRepository;
    LoginRepository authRepository;
    LiveData<List<ProductInstanceGroupInfo>> list;

    public ProductExpirationNotificationViewModel(@NonNull Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        authRepository = LoginRepository.getInstance(application);
        list = pantryRepository.getLiveInfoOfAll(null, null);
    }

    public LiveData<List<ProductInstanceGroupInfo>> getAllProductInfo() {
        return list;
    }

    public LiveData<LoggedAccount> getLoggedAccount() {
        return authRepository.getLoggedInUser();
    }
}
