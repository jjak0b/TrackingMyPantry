package com.jjak0b.android.trackingmypantry.ui.util;

import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.data.model.entities.Place;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;

public class PlaceAdapter {

    public static Feature from(@NonNull Place place){
        return place.getFeature();
    }

    public static Place from(@NonNull CarmenFeature feature) {
        return new Place(feature.id(), Feature.fromGeometry(feature.geometry()), feature.text() );
    }
}
