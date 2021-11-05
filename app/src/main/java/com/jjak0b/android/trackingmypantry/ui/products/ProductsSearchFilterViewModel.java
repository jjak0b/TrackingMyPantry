package com.jjak0b.android.trackingmypantry.ui.products;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.model.repositories.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.filters.SearchFilterState;
import com.jjak0b.android.trackingmypantry.data.model.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.ui.SearchFilterViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductsSearchFilterViewModel extends SearchFilterViewModel {

    private PantryRepository pantryRepository;
    private LiveEvent<SearchFilterState> onSearchEvent;
    private MutableLiveData<List<ProductTag>> mSearchTags;
    private LiveData<List<ProductTag>> mSearchTagsSuggestions;

    public ProductsSearchFilterViewModel(Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        onSearchEvent = new LiveEvent<>();
        mSearchTags = new MutableLiveData<>(new ArrayList<>());
        mSearchTagsSuggestions = pantryRepository.getAllProductTags();
    }

    @Override
    public void search() {
        SearchFilterState s = new SearchFilterState();
        s.query = getSearchQuery().getValue();
        if( s.query != null )
            s.query = s.query.trim();

        s.searchTags = new ArrayList<>(mSearchTags.getValue()
                .stream()
                .map(tag -> tag.getId())
                .collect(Collectors.toList())
        );
        onSearchEvent.postValue(s);
    }

    public void reset() {
        setSearchQuery(null);
        setSearchTags(new ArrayList<>());
        onSearchEvent.setValue(null);
    }

    public LiveEvent<SearchFilterState> onSearch() {
        return onSearchEvent;
    }

    public LiveData<List<ProductTag>> getSearchTagsSuggestions() {
        return mSearchTagsSuggestions;
    }

    public LiveData<List<ProductTag>> getSearchTags() {
        return mSearchTags;
    }

    public void setSearchTags(List<ProductTag> searchTags) {
        if(!Objects.equals(searchTags, this.mSearchTags.getValue() )){
            this.mSearchTags.setValue(searchTags);
        }
    }
}