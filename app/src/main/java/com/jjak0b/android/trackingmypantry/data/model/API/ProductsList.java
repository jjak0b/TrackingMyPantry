package com.jjak0b.android.trackingmypantry.data.model.API;

import com.google.gson.annotations.Expose;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.TokenizedItem;

import java.util.List;

public class ProductsList extends TokenizedItem {

    @Expose
    List<Product> products;

    public ProductsList(String token) {
        super(token);
    }

    public List<Product> getProducts() { return products; }
}