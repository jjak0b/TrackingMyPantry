package com.jjak0b.android.trackingmypantry.data.db;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.jjak0b.android.trackingmypantry.data.PlaceSearchSuggestionBuilder;
import com.jjak0b.android.trackingmypantry.data.db.entities.Address;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.mapbox.geojson.Point;
import com.mapbox.search.result.SearchAddress;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;

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
    public static String FeatureToJson(Point feature){
        return feature.toJson();
    }

    @TypeConverter
    public static Point JsonToFeature(String geoJson){
        return Point.fromJson(geoJson);
    }

    public static SearchAddress fromRAddress(Address address) {
        SearchAddress.Builder builder = new SearchAddress.Builder();
        if( address.country != null ) builder.country(address.country);
        if( address.region != null ) builder.region(address.region);
        if( address.postcode != null ) builder.postcode(address.postcode);
        if( address.locality != null ) builder.locality(address.locality);
        if( address.neighborhood != null ) builder.neighborhood(address.neighborhood);
        if( address.district != null ) builder.district(address.district);
        if( address.street != null ) builder.street(address.street);
        if( address.houseNumber != null ) builder.houseNumber(address.houseNumber);
        if( address.place != null ) builder.place(address.place);

        return builder.build();
    }

    public static Place fromResult(SearchResult result) {
        Address address = new Address();
        SearchAddress resultAddress = result.getAddress();
        if( resultAddress != null ) {
            address.country = resultAddress.getCountry();
            address.district = resultAddress.getDistrict();
            address.houseNumber = resultAddress.getHouseNumber();
            address.locality = resultAddress.getLocality();
            address.neighborhood = resultAddress.getNeighborhood();
            address.place = resultAddress.getPlace();
            address.postcode = resultAddress.getPostcode();
            address.region = resultAddress.getRegion();
            address.street = resultAddress.getStreet();
        }

        return new Place(result.getId(), result.getCoordinate(), result.getName(), address);
    }

    public static PlaceSearchSuggestionBuilder.APISearchSuggestion fromSuggestion(SearchSuggestion suggestion) {
        return PlaceSearchSuggestionBuilder.create(suggestion);
    }

    public static PlaceSearchSuggestionBuilder.FallbackSearchSuggestion fromPlace(Place place) {
        return PlaceSearchSuggestionBuilder.create(place);
    }
}
