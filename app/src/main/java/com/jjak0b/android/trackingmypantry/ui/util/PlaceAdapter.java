package com.jjak0b.android.trackingmypantry.ui.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.data.model.Place;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.gson.GeometryGeoJson;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.data.converter.CarmenFeatureConverter;

public class PlaceAdapter {

    public static Feature from(@NonNull Place place){
        return place.getFeature();
    }

    public static Place from(@NonNull CarmenFeature feature) {
        return new Place(feature.id(), Feature.fromGeometry(feature.geometry()), feature.text() );
    }
}
