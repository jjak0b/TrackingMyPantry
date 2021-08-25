package com.jjak0b.android.trackingmypantry.ui.register_product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Objects;

public class PageViewModel extends ViewModel {

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>( 0 );

    private MutableLiveData<Integer> maxTabCount = new MutableLiveData<>( 1 );

    public void setPageIndex(Integer index) {
        if( !Objects.equals( mIndex.getValue(), index ) ){
            mIndex.setValue(index);
        }
    }

    public LiveData<Integer> getPageIndex(){
        return mIndex;
    }

    public LiveData<Integer> getMaxNavigableTabCount() {
        return maxTabCount;
    }

    public void setMaxNavigableTabCount( Integer c ) {
        maxTabCount.setValue( c );
    }

    public boolean canSelectNextTab() {
        return mIndex.getValue() + 1 < maxTabCount.getValue();
    }
}