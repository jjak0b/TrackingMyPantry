package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.purchase_locations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;

import java.util.List;
import java.util.Objects;

public class PurchasesInPlaceViewModel extends ViewModel {

    private MutableLiveData<Boolean> mIsLoadingState;
    private MutableLiveData<List<PurchaseInfo>> mPurchases;

    public PurchasesInPlaceViewModel() {
        super();
        mPurchases = new MutableLiveData<>(null);
        mIsLoadingState = new MutableLiveData<>(false);
    }

    /**
     * set the purchases, assuming they are already ordered by date
     * @param purchases
     */
    void setPurchases(List<PurchaseInfo> purchases){
        if(Objects.equals( purchases, this.mPurchases.getValue())) return;
        mIsLoadingState.setValue(true);
        // we suppose purchases are already sorted
        mPurchases.postValue(purchases);
        mIsLoadingState.postValue(false);
    }

    public LiveData<List<PurchaseInfo>> getPurchases() {
        return mPurchases;
    }

    public LiveData<Boolean> isLoading() {
        return this.mIsLoadingState;
    }
}