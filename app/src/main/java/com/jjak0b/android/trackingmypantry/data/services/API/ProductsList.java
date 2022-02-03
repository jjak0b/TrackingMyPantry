package com.jjak0b.android.trackingmypantry.data.services.API;

import com.google.gson.annotations.Expose;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;

import java.util.List;

public class ProductsList extends TokenizedItem {

    @Expose
    List<Product> products;

    public ProductsList(String token) {
        super(token);
    }

    public List<Product> getProducts() { return products; }
}