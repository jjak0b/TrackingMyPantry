package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.purchase_locations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jjak0b.android.trackingmypantry.data.model.PurchaseInfo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PurchasesInPlaceViewModel extends ViewModel {

    private MutableLiveData<Boolean> mIsLoadingState;
    private MutableLiveData<List<PurchaseInfo>> mPurchases;
    private static final int nTHREADS = 1;
    private static final ExecutorService executor = Executors.newFixedThreadPool(nTHREADS);

    public PurchasesInPlaceViewModel() {
        super();
        mPurchases = new MutableLiveData<>(null);
        mIsLoadingState = new MutableLiveData<>(false);
    }

    void setPurchases(List<PurchaseInfo> purchases){
        if(Objects.equals( purchases, this.mPurchases.getValue())) return;
        mIsLoadingState.setValue(true);
        // Sort and set the items from a separate thread because it could be too heavy for main thread
        executor.submit(() -> {
            Collections.sort( purchases,(o1, o2) -> (int) (o1.getPurchaseDate().getTime() - o2.getPurchaseDate().getTime()));
            mIsLoadingState.postValue(false);
            this.mPurchases.postValue(purchases);
        });
    }

    public LiveData<List<PurchaseInfo>> getPurchases() {
        return mPurchases;
    }

    public LiveData<Boolean> isLoading() {
        return this.mIsLoadingState;
    }
}