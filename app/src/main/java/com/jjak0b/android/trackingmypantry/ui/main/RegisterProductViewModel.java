package com.jjak0b.android.trackingmypantry.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jjak0b.android.trackingmypantry.data.dataSource.LoginDataSource;
import com.jjak0b.android.trackingmypantry.data.model.Product;

public class RegisterProductViewModel extends ViewModel {

    protected MutableLiveData<Product> product = new MutableLiveData<>();


}