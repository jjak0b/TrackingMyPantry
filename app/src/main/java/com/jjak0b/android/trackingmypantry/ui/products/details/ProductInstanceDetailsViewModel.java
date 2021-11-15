package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.repositories.PantryRepository;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ProductInstanceDetailsViewModel extends AndroidViewModel {

    protected PantryRepository pantryRepository;
    private MutableLiveData<Integer> mQuantity;
    private MutableLiveData<Date> mExpireDate;
    private MutableLiveData<Pantry> mPantry;
    private LiveEvent<Boolean> onSave;

    public ProductInstanceDetailsViewModel(@NonNull Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        mPantry = new MutableLiveData<>(null);
        mQuantity = new MutableLiveData<>(null);
        mExpireDate = new MutableLiveData<>(null);
        onSave = new LiveEvent<>();
    }

    public LiveData<Boolean> onSave() {
        return onSave;
    }

    public void save() {
        onSave.setValue(true);
        onSave.postValue(false);
    }

    public LiveData<Pantry> getPantry() {
        return mPantry;
    }

    public LiveData<List<Pantry>> getAvailablePantries() {
        return pantryRepository.getPantries();
    }

    public void setPantry(Pantry pantry) {
        if(!Objects.equals(pantry, this.mPantry.getValue()))
            this.mPantry.setValue(pantry);
    }

    public LiveData<Integer> getQuantity() {
        return mQuantity;
    }

    public void setQuantity(int quantity) {
        if(!Objects.equals(quantity, this.mQuantity.getValue()))
            this.mQuantity.setValue(quantity);
    }

    public LiveData<Date> getExpireDate() {
        return mExpireDate;
    }

    public void setExpireDate(Date expireDate) {
        if(!Objects.equals(expireDate, this.mExpireDate.getValue()))
            this.mExpireDate.setValue(expireDate);;
    }
}
