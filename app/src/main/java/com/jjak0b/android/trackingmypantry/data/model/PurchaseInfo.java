package com.jjak0b.android.trackingmypantry.data.model;


import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

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

}
