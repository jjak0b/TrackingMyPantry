package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.Nullable;

import java.util.Objects;

public class Address {

    /**
     * Address house number.
     */
    @Nullable
    public String houseNumber;


    /**
     * Address street.
     */
    @Nullable
    public String street;

    /**
     * Address neighborhood.
     */
    @Nullable
    public String neighborhood;

    /**
     * Address locality.
     */
    @Nullable
    public String locality;

    /**
     * Address postcode.
     */
    @Nullable
    public String postcode;

    /**
     * Address place.
     */
    @Nullable
    public String place;

    /**
     * Address district.
     */
    @Nullable
    public String district;

    /**
     * Address region.
     */
    @Nullable
    public String region;

    /**
     * Address country.
     */
    @Nullable
    public String country;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address address = (Address) o;
        return Objects.equals(place, address.place)
            && Objects.equals(locality, address.locality)
            && Objects.equals(houseNumber, address.houseNumber)
            && Objects.equals(street, address.street)
            && Objects.equals(neighborhood, address.neighborhood)
            && Objects.equals(postcode, address.postcode)
            && Objects.equals(district, address.district)
            && Objects.equals(region, address.region)
            && Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseNumber, street, neighborhood, locality, postcode, place, district, region, country);
    }
}
