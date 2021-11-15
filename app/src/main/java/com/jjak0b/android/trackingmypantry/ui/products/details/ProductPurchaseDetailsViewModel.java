package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.repositories.PantryRepository;

import java.util.Date;
import java.util.Objects;

public class ProductPurchaseDetailsViewModel extends AndroidViewModel {

    protected PantryRepository pantryRepository;
    private MutableLiveData<Float> cost;
    private MutableLiveData<Date> purchaseDate;
    private MutableLiveData<Place> purchasePlace;
    private LiveEvent<Boolean> onSave;


    public ProductPurchaseDetailsViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> onSave() {
        return onSave;
    }

    public void save() {
        onSave.setValue(true);
        onSave.postValue(false);
    }


    public LiveData<Float> getCost() {
        return cost;
    }

    public void setCost(float cost) {
        if(!Objects.equals(cost, this.cost.getValue()))
            this.cost.setValue(cost);
    }

    public LiveData<Date> getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        if(!Objects.equals(purchaseDate, this.purchaseDate.getValue()))
            this.purchaseDate.setValue(purchaseDate);
    }

    public LiveData<Place> getPurchasePlace() {
        return purchasePlace;
    }

    public void setPurchasePlace(Place purchasePlace) {
        if(!Objects.equals(purchasePlace, this.purchasePlace.getValue()))
            this.purchasePlace.setValue(purchasePlace);
    }
}
