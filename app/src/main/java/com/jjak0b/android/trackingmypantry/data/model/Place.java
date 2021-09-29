package com.jjak0b.android.trackingmypantry.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;

@Entity(
        tableName = "places"
)
public class Place implements GeoJson {
    @PrimaryKey
    @NonNull
    private String id;

    private String name;

    @NonNull
    private Feature feature;

    public Place(@NonNull String id, @NonNull Feature feature, String name) {
        this.id = id;
        this.feature = feature;
        this.name = name;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @NonNull
    public Feature getFeature() {
        return feature;
    }

    @Override
    public String type() {
        return feature.type();
    }

    @Override
    public String toJson() {
        return feature.toJson();
    }

    @Override
    public BoundingBox bbox() {
        return feature.bbox();
    }
}
