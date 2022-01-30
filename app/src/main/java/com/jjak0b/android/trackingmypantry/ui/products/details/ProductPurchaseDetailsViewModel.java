package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.db.relationships.PurchaseInfoWithPlace;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;
import com.jjak0b.android.trackingmypantry.ui.util.ISavable;
import com.jjak0b.android.trackingmypantry.ui.util.Savable;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ProductPurchaseDetailsViewModel extends AndroidViewModel implements ISavable<PurchaseInfoWithPlace> {

    private MutableLiveData<Resource<Float>> mCost;
    private MutableLiveData<Resource<Date>> mPurchaseDate;
    private MutableLiveData<Resource<Place>> mPurchasePlace;
    private Savable<PurchaseInfoWithPlace> savable;


    public ProductPurchaseDetailsViewModel(@NonNull Application application) {
        super(application);
        mCost = new MutableLiveData<>(Resource.success(0f));
        mPurchaseDate = new MutableLiveData<>(Resource.success(new Date()));
        mPurchasePlace = new MutableLiveData<>(Resource.success(null));
        savable = new Savable<>();

        reset();
    }

    public void reset() {
        setCost(0f);
        setPurchaseDate(null);
        setPurchasePlace(null);
    }

    private boolean updateValidity(boolean updateSavable) {
        boolean isValid = true;

        isValid = isValid && Transformations.onValid(getCost().getValue(), null);
        isValid = isValid && Transformations.onValid(getPurchasePlace().getValue(), null);
        isValid = isValid && Transformations.onValid(getPurchaseDate().getValue(), null);

        if( updateSavable )
            savable.enableSave(isValid);
        return isValid;
    }

    public LiveData<Resource<Float>> getCost() {
        return mCost;
    }

    public void setCost(float cost) {
        if(!Objects.equals(cost, this.mCost.getValue().getData())) {
            this.mCost.setValue(Resource.success(cost));
            updateValidity(true);
        }
    }

    public LiveData<Resource<Date>> getPurchaseDate() {
        return mPurchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        Calendar calendar = Calendar.getInstance();
        if( purchaseDate != null) {
            calendar.setTime(purchaseDate);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // clear time and recreate using only day,month,year without other details
        calendar.setTimeInMillis(0);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        purchaseDate = calendar.getTime();

        if(!Objects.equals(purchaseDate, this.mPurchaseDate.getValue().getData())) {
            this.mPurchaseDate.setValue(Resource.success(purchaseDate));
            updateValidity(true);
        }
    }

    public LiveData<Resource<Place>> getPurchasePlace() {
        return mPurchasePlace;
    }

    public void setPurchasePlace(Place purchasePlace) {
        if(!Objects.equals(purchasePlace, this.mPurchasePlace.getValue().getData())) {
            this.mPurchasePlace.setValue(Resource.success(purchasePlace));
            updateValidity(true);
        }
    }

    @Override
    public LiveData<Boolean> canSave() {
        return savable.canSave();
    }

    @Override
    public void saveComplete() {
        savable.saveComplete();
    }

    @Override
    public void save() {

        savable.onSaved().removeSource(savable.onSave());
        savable.onSaved().addSource(savable.onSave(), aBoolean -> {
            if (aBoolean) {
                savable.setSavedResult(Resource.loading(null));
                return;
            }
            savable.onSaved().removeSource(savable.onSave());
            savable.setSavedResult(Resource.loading(null));
            if( updateValidity(false) ) {
                PurchaseInfoWithPlace purchaseInfoWPlace = new PurchaseInfoWithPlace();
                purchaseInfoWPlace.place = getPurchasePlace().getValue().getData();
                purchaseInfoWPlace.info = new PurchaseInfo(
                        getCost().getValue().getData(),
                        getPurchaseDate().getValue().getData(),
                        purchaseInfoWPlace.place != null ? purchaseInfoWPlace.place.getId() : null
                );
                savable.setSavedResult(Resource.success(purchaseInfoWPlace));
            }
            else {
                savable.setSavedResult(Resource.error(
                        new FormException(getApplication().getString(R.string.form_error_invalid)),
                        null
                ));
            }
        });

        savable.save();
    }

    @Override
    public MediatorLiveData<Boolean> onSave() {
        return savable.onSave();
    }

    @Override
    public MediatorLiveData<Resource<PurchaseInfoWithPlace>> onSaved() {
        return savable.onSaved();
    }
}
