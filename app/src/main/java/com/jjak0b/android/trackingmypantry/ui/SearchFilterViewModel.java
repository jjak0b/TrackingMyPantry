package com.jjak0b.android.trackingmypantry.ui;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Objects;

public abstract class SearchFilterViewModel extends AndroidViewModel {
    private MutableLiveData<String> searchQuery;
    private static final String TAG = "SearchFilterViewModel";
    public SearchFilterViewModel(Application application){
        super(application);
        searchQuery = new MutableLiveData<>(null);
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        if(!Objects.equals(searchQuery, this.searchQuery.getValue() )){
            Log.d(TAG, "updating query " + searchQuery);
            this.searchQuery.setValue(searchQuery);
        }
    }

    public abstract void search();
}
