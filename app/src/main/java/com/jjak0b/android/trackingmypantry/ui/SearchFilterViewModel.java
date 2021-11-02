package com.jjak0b.android.trackingmypantry.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class SearchFilterViewModel extends AndroidViewModel {
    private MutableLiveData<String> searchQuery;

    public SearchFilterViewModel(Application application){
        super(application);
        searchQuery = new MutableLiveData<>(null);
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        if(!Objects.equals(searchQuery, this.searchQuery.getValue() )){
            this.searchQuery.setValue(searchQuery);
        }
    }

    public abstract void search();
}
