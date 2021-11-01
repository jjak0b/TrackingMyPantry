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
    private MutableLiveData<List<String>> searchTags;

    public SearchFilterViewModel(Application application){
        super(application);
        searchQuery = new MutableLiveData<>(null);
        searchTags = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<String>> getSearchTags() {
        return searchTags;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        if(!Objects.equals(searchQuery, this.searchQuery.getValue() )){
            this.searchQuery.setValue(searchQuery);
        }
    }

    public void setSearchTags(List<String> searchTags) {
        if(!Objects.equals(searchTags, this.searchTags.getValue() )){
            this.searchTags.setValue(searchTags);
        }
    }

    public abstract void search();
}
