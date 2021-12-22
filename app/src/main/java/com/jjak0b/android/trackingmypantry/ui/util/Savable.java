package com.jjak0b.android.trackingmypantry.ui.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.api.Resource;

public class Savable<T> implements ISavable<T> {

    private LiveEvent<Boolean> onSave;
    private LiveEvent<Resource<T>> mSavedResult;
    private MutableLiveData<Boolean> mCanSave;

    public Savable() {
        this.mCanSave = new MutableLiveData<>(false);
        this.onSave = new LiveEvent<>();
        this.mSavedResult = new LiveEvent<>();
    }

    public LiveData<Boolean> canSave() {
        return mCanSave;
    }

    public void saveComplete() {
        onSave.postValue(false);
    }


    public void save() {
        onSave.setValue(true);
    }

    /**
     *
     * @return true if caller should save some stuff, false when save has been done
     */
    public MediatorLiveData<Boolean> onSave() {
        return onSave;
    }

    public void enableSave(boolean canSave) {
        mCanSave.setValue(true);
    }

    /**
     *
     * @param result
     */
    public void setSavedResult(Resource<T> result) {
        mSavedResult.setValue(result);
    }

    public MediatorLiveData<Resource<T>> onSaved() {
        return mSavedResult;
    }
}
