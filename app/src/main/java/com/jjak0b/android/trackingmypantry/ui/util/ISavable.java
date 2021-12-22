package com.jjak0b.android.trackingmypantry.ui.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;

public interface ISavable<T> {

    LiveData<Boolean> canSave();

    void saveComplete();

    void save();

    MediatorLiveData<Boolean> onSave();

    MediatorLiveData<Resource<T>> onSaved();

}
