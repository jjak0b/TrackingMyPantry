package com.jjak0b.android.trackingmypantry.data.model;


import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Objects;

public class PurchaseInfo {
    // @PrimaryKey(autoGenerate = true)
    // long id;
    Date purchaseDate;
    @Embedded
    GeoLocation purchaseLocation;
    float cost;

    public PurchaseInfo(float cost, Date purchaseDate, GeoLocation purchaseLocation ) {
        this.purchaseDate = purchaseDate;
        this.purchaseLocation = purchaseLocation;
        this.cost = cost;
        this.purchaseLocation = purchaseLocation;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public GeoLocation getPurchaseLocation() {
        return purchaseLocation;
    }

    public void setPurchaseLocation(GeoLocation purchaseLocation) {
        this.purchaseLocation = purchaseLocation;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseInfo that = (PurchaseInfo) o;
        return Float.compare(that.cost, cost) == 0 &&
                Objects.equals(purchaseDate, that.purchaseDate) &&
                Objects.equals(purchaseLocation, that.purchaseLocation);
    }

}
