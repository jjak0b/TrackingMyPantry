package com.jjak0b.android.trackingmypantry.ui.products;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.filters.SearchFilterState;
import com.jjak0b.android.trackingmypantry.ui.SearchFilterViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductsSearchFilterViewModel extends SearchFilterViewModel {

    private LiveEvent<SearchFilterState> onSearchEvent;
    private MutableLiveData<List<ProductTag>> mSearchTags;

    public ProductsSearchFilterViewModel(Application application) {
        super(application);

        onSearchEvent = new LiveEvent<>();
        mSearchTags = new MutableLiveData<>(new ArrayList<>());

    }

    @Override
    public void search() {
        SearchFilterState s = new SearchFilterState();
        s.query = getSearchQuery().getValue();
        if( s.query != null )
            s.query = s.query.trim();

        s.searchTags = mSearchTags.getValue() != null
                ? new ArrayList<>(mSearchTags.getValue())
                    .stream()
                    .map(ProductTag::getId)
                    .collect(Collectors.toList())
                : new ArrayList<>(0);
        onSearchEvent.postValue(s);
    }

    public void reset() {
        setSearchQuery(null);
        setSearchTags(null);
        onSearchEvent.setValue(null);
    }

    public LiveEvent<SearchFilterState> onSearch() {
        return onSearchEvent;
    }

    public LiveData<List<ProductTag>> getSearchTags() {
        return mSearchTags;
    }

    public void setSearchTags(List<ProductTag> searchTags) {
        if(!Objects.equals(searchTags, this.mSearchTags.getValue() )){
            if( searchTags == null ) {
                searchTags = new ArrayList<>(0);
            }
            this.mSearchTags.setValue(searchTags);
        }
    }
}