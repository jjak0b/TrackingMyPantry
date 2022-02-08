package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Point;

import java.util.Objects;

@Entity(
        tableName = "places"
)
public class Place implements GeoJson {
    @PrimaryKey
    @NonNull
    private String id;

    private String name;

    @Embedded
    private Address address;

    private Point feature;

    public Place(@NonNull String id, Point feature, String name, Address address) {
        this.id = id;
        this.feature = feature;
        this.name = name;
        this.address = address;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @NonNull
    public Point getFeature() {
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

    public Address getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "Place{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", feature=" + feature +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return Objects.equals(id, place.id)
            && Objects.equals(name, place.name)
            && Objects.equals(feature, place.feature)
            && Objects.equals(address, place.address );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, feature, address);
    }
}
