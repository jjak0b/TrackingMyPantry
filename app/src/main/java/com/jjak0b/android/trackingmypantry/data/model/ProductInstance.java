package com.jjak0b.android.trackingmypantry.data.model;

import android.location.Location;

import java.util.Date;

public class ProductInstance {
    int id;
    int productId;
    Date purchaseDate;
    float cost;
    Date expiryDate;
    Location boughtLocation;
    float currentAmountPercent;
}
