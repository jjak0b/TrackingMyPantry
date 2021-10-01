package com.jjak0b.android.trackingmypantry.ui.util;

import androidx.annotation.Nullable;

import com.jjak0b.android.trackingmypantry.data.model.Place;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.MultiPoint;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CoordinateBounds;

public class GeoUtils {

    @Nullable
    public static Point getCenter(GeoJson geoJson) {

        if(geoJson instanceof Feature && ((Feature)geoJson).geometry() != null ){
            Feature feature = (Feature)geoJson;
            return Point.fromJson(feature.geometry().toJson());
        }
        else if( geoJson.bbox() != null ){
            return getCenter(geoJson.bbox());
        }
        else {
            return null;
        }
    }

    public static Point getCenter(BoundingBox bbox) {
        return Point.fromLngLat( (bbox.east()+bbox.west())/2.0, (bbox.north()+bbox.south())/2.0 );
    }
}
