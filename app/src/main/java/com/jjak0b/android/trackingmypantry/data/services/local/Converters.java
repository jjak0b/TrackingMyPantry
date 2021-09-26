package com.jjak0b.android.trackingmypantry.data.services.local;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.jjak0b.android.trackingmypantry.ui.util.PlaceAdapter;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.gson.GeometryGeoJson;

import java.util.Date;

@ProvidedTypeConverter
public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String GeoJsonToJson(GeoJson geometry){
        return geometry.toJson();
    }

    @TypeConverter
    public static GeoJson GeoJsonFromJson(String geoJson){
        return GeometryGeoJson.fromJson(geoJson);
    }
}
