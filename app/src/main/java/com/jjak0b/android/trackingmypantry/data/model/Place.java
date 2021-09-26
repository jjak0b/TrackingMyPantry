package com.jjak0b.android.trackingmypantry.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mapbox.geojson.GeoJson;
@Entity(
        tableName = "places"
)
public class Place {
    @PrimaryKey
    @NonNull
    String id;

    String name;

    GeoJson geometry;

    public Place(@NonNull String id, GeoJson geometry, String name) {
        this.id = id;
        this.geometry = geometry;
        this.name = name;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public GeoJson getGeometry() {
        return geometry;
    }
}
