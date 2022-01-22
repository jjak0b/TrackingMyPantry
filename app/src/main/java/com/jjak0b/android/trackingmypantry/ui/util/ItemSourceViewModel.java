package com.jjak0b.android.trackingmypantry.ui.util;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;

public class ItemSourceViewModel<I> extends AndroidViewModel {

    private final MediatorLiveData<Resource<I>> mItem = new MediatorLiveData<>();
    private LiveData<Resource<I>> mItemSource;

    public ItemSourceViewModel(Application application) {
        super(application);
        setItemSource(null);
    }

    public LiveData<Resource<I>> getItem() {
        return mItem;
    }

    @MainThread
    public void setItemSource( LiveData<Resource<I>> mSource ) {
        if( mSource != null ) {
            if( mItemSource != null ) {
                mItem.removeSource(mItemSource);
            }
            mItemSource = mSource;
            mItem.addSource(mSource, mItem::setValue );
        }
        else {
            setItemSource(new MutableLiveData<>(Resource.loading(null)));
        }
    }
}
