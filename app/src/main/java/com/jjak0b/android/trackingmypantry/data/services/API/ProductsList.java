package com.jjak0b.android.trackingmypantry.data.services.API;

import com.google.gson.annotations.Expose;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;

import java.util.List;

public class ProductsList extends TokenizedItem {

    @Expose
    List<UserProduct> products;

    public ProductsList(String token) {
        super(token);
    }

    public List<UserProduct> getProducts() { return products; }
}