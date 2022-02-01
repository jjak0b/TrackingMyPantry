package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductInstanceGroupInfo;
import com.jjak0b.android.trackingmypantry.data.repositories.PantriesRepository;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;
import com.jjak0b.android.trackingmypantry.ui.util.ISavable;
import com.jjak0b.android.trackingmypantry.ui.util.Savable;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ProductInstanceDetailsViewModel extends AndroidViewModel implements ISavable<ProductInstanceGroupInfo> {

    protected PantriesRepository pantriesRepository;
    private MutableLiveData<Resource<Integer>> mQuantity;
    private MutableLiveData<Resource<Date>> mExpireDate;
    private MutableLiveData<Resource<Pantry>> mPantry;
    private Savable<ProductInstanceGroupInfo> savable;
    private LiveData<Resource<List<Pantry>>> mAvailablePantries;
    private AppExecutors appExecutors;

    public ProductInstanceDetailsViewModel(@NonNull Application application) {
        super(application);
        appExecutors = AppExecutors.getInstance();
        pantriesRepository = PantriesRepository.getInstance(application);
        mAvailablePantries = pantriesRepository.getPantries();

        mPantry = new MutableLiveData<>(Resource.loading(Pantry.creteDummy(null)));
        mQuantity = new MutableLiveData<>(Resource.success(1));
        mExpireDate = new MutableLiveData<>(Resource.success(new Date()));

        savable = new Savable<>();

        reset();
    }

    boolean updateValidity(boolean updateSavable) {
        boolean isValid = true;

        isValid = isValid && Transformations.onValid(getPantry().getValue(), null);
        isValid = isValid && Transformations.onValid(getQuantity().getValue(), null);
        isValid = isValid && Transformations.onValid(getExpireDate().getValue(), null);

        if( updateSavable )
            savable.enableSave(isValid);

        return isValid;
    }

    public MediatorLiveData<Boolean> onSave() {
        return savable.onSave();
    }

    @Override
    public MediatorLiveData<Resource<ProductInstanceGroupInfo>> onSaved() {
        return savable.onSaved();
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
                ProductInstanceGroupInfo info = new ProductInstanceGroupInfo();
                info.pantry = getPantry().getValue().getData();

                info.group = new ProductInstanceGroup();
                info.group.setQuantity(getQuantity().getValue().getData());
                info.group.setExpiryDate(getExpireDate().getValue().getData());

                savable.setSavedResult(Resource.success(info));
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

    public void reset() {
        setExpireDate(null);
        setPantry(null);
        setQuantity(1);
    }

    public LiveData<Resource<Pantry>> getPantry() {
        return mPantry;
    }

    public LiveData<Resource<List<Pantry>>> getAvailablePantries() {
        return mAvailablePantries;
    }

    public void setPantry(Pantry pantry) {
        if(!Objects.equals(pantry, this.mPantry.getValue().getData())) {

            if( pantry == null || TextUtils.isEmpty(pantry.getName()) ) {
                this.mPantry.setValue(Resource.error(new FormException(
                        getApplication().getString(R.string.field_error_empty)),
                        null
                ));
            }
            else {
                this.mPantry.setValue(Resource.success(pantry));
            }
            updateValidity(true);
        }
    }

    public LiveData<Resource<Integer>> getQuantity() {
        return mQuantity;
    }

    public void setQuantity(int quantity) {

        if(!Objects.equals(quantity, this.mQuantity.getValue().getData())) {
            if( quantity <= 0 ) {
                this.mQuantity.setValue(Resource.error(
                        new FormException(getApplication().getString(R.string.field_error_invalid)),
                        null
                ));
            }
            else {
                this.mQuantity.setValue(Resource.success(quantity));
            }
            updateValidity(true);
        }
    }

    public void setQuantity(String value) {
        try {
            setQuantity(Integer.parseInt( value ));
        }
        catch (NumberFormatException e) {
            this.mQuantity.setValue(Resource.error(
                    new FormException(getApplication().getString(R.string.field_error_invalid)),
                    null
            ));
            updateValidity(true);
        }
    }

    public LiveData<Resource<Date>> getExpireDate() {
        return mExpireDate;
    }

    public void setExpireDate(Date expireDate) {
        if( expireDate == null ) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            expireDate = calendar.getTime();
        }

        if(!Objects.equals(expireDate, this.mExpireDate.getValue().getData())) {
            this.mExpireDate.setValue(Resource.success(expireDate));
            updateValidity(true);
        }
    }
}
