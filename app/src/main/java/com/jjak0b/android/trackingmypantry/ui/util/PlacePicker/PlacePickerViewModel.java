package com.jjak0b.android.trackingmypantry.ui.util.PlacePicker;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.data.PlaceSearchSuggestion;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.repositories.PlacesRepository;
import com.jjak0b.android.trackingmypantry.ui.SearchFilterViewModel;

import java.util.Collections;
import java.util.List;

public class PlacePickerViewModel extends SearchFilterViewModel {

    private PlacesRepository placesRepository;
    private MediatorLiveData<Resource<List<? extends PlaceSearchSuggestion>>> placeSuggestions;
    private LiveData<Resource<List<? extends PlaceSearchSuggestion>>> placeSuggestionsSource;

    public PlacePickerViewModel(@NonNull Application application) {
        super(application);
        placesRepository = PlacesRepository.getInstance(application);
        placeSuggestions = new MediatorLiveData<>();
        placeSuggestionsSource = new MutableLiveData<>(Resource.loading(Collections.emptyList()));


    }

    @Override
    public void search() {
        placeSuggestions.addSource(getSearchQuery(), query -> {
            placeSuggestions.removeSource(getSearchQuery());

            Log.d("test", "querying " + query);

            if( placeSuggestionsSource != null ) {
                placeSuggestions.removeSource(placeSuggestionsSource);
                placeSuggestionsSource = null;
            }

            if(!TextUtils.isEmpty(query)) {
                // attach a new results source
                LiveData<Resource<List<? extends PlaceSearchSuggestion>>> source = placesRepository.search(query);
                placeSuggestionsSource = source;

                placeSuggestions.addSource(source, placeSuggestions::setValue );
            }
        });

    }

    public LiveData<Resource<List<? extends PlaceSearchSuggestion>>> getPlaceSuggestions() {
        return placeSuggestions;
    };

    public LiveData<Resource<Place>> getPlace(PlaceSearchSuggestion suggestion) {
        return placesRepository.get(suggestion);
    }
}
