package com.jjak0b.android.trackingmypantry.ui.util;

import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.data.model.Place;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Geometry;

public class PlaceAdapter {

    public static Place from(@NonNull CarmenFeature place){
        return new Place( place.id(), place.geometry(), place.text() != null ? place.text() : place.placeName() );
    }

    public static CarmenFeature from(@NonNull Place place){
        return CarmenFeature.builder()
                .id(place.getId())
                .text(place.getName())
                .geometry(new Geometry() {
                    @Override
                    public String type() {
                        return place.getGeometry().type();
                    }

                    @Override
                    public String toJson() {
                        return place.getGeometry().toJson();
                    }

                    @Override
                    public BoundingBox bbox() {
                        return place.getGeometry().bbox();
                    }
                })
                .build();
    }

}
