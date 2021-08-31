package com.jjak0b.android.trackingmypantry.ui.maps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;

public class PlacesPluginViewModel extends ViewModel {
    private MutableLiveData<CarmenFeature> mPlace;

    public PlacesPluginViewModel() {
        mPlace = new MutableLiveData<>(null);
    }

    void setPlace(CarmenFeature place ){
        mPlace.setValue(place);
    }

    LiveData<CarmenFeature> getPlace() {
        return mPlace;
    }
}
