package com.jjak0b.android.trackingmypantry.ui.util;

import androidx.annotation.Nullable;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Point;

public class GeoUtils {

    @Nullable
    public static Point getCenter(GeoJson geoJson) {

        if(geoJson instanceof Point ){
            return (Point) geoJson;
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
