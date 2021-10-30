package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.app.Application;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.SearchFilterState;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.ui.SearchFilterViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ProductsSearchFilterViewModel extends SearchFilterViewModel {

    private PantryRepository pantryRepository;
    private LiveData<List<ProductTag>> mSuggestions;
    private LiveEvent<SearchFilterState> onSearchEvent;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    public ProductsSearchFilterViewModel(Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        onSearchEvent = new LiveEvent<>();
/*
        mSuggestions = Transformations.switchMap(pantryRepository.getAllProductTags(), new Function<List<ProductTag>, LiveData<List<String>>>() {
            @Override
            public LiveData<List<String>> apply(List<ProductTag> productTags) {
                MutableLiveData<List<String>> suggestions = new MutableLiveData<>();
                if( productTags == null ){
                    suggestions.setValue(new ArrayList<>(0));
                }
                else {
                    executor.submit(() -> {
                        List<String> strings = productTags.stream()
                                .map(tag -> tag.toString() )
                                .collect(Collectors.toList());
                        suggestions.postValue(strings);
                    });
                }
                return suggestions;
            }
        });*/

        mSuggestions = pantryRepository.getAllProductTags();
    }

    public LiveData<List<ProductTag>> getSuggestions() {
        return mSuggestions;
    }

    @Override
    public void search() {
        SearchFilterState s = new SearchFilterState();
        s.query = getSearchQuery().getValue();
        if( s.query != null )
            s.query = s.query.trim();
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
}