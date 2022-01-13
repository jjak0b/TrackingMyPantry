package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Objects;

public class PageViewModel extends ViewModel {

    private MutableLiveData<Pair<Integer, Integer>> mIndex;
    private MutableLiveData<Integer> maxTabCount;

    public PageViewModel() {
        super();
        mIndex = new MutableLiveData<>(Pair.create(0,0));
        maxTabCount = new MutableLiveData<>( 1 );
    }

    /**
     * Set the current page index
     * @param index
     */
    public void setPageIndex(Integer index) {
        if( !Objects.equals( mIndex.getValue().first, index ) ){
            mIndex.setValue(Pair.create( index, mIndex.getValue().first) );
        }
    }

    /**
     *
     * @return a pair lived data of (current index, previous index)
     */
    public LiveData<Pair<Integer, Integer>> getPageIndex(){
        return mIndex;
    }

    public LiveData<Integer> getMaxNavigableTabCount() {
        return maxTabCount;
    }

    public void setMaxNavigableTabCount( Integer c ) {
        if( !Objects.equals(maxTabCount.getValue(), c ) ){
            maxTabCount.setValue( c );
        }
    }

    public boolean canSelectNextTab() {
        return mIndex.getValue().first + 1 < maxTabCount.getValue();
    }
}