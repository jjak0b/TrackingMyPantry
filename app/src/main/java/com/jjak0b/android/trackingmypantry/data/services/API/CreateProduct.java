package com.jjak0b.android.trackingmypantry.data.services.API;

import com.google.gson.annotations.Expose;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;

import androidx.annotation.NonNull;

public class CreateProduct extends Product {

    @Expose
    String token;

    @Expose
    boolean test;

    public CreateProduct( @NonNull Product product, @NonNull String token) {
        super( product );

        this.test = true; // TODO: remove on release
        this.token = token;
    }
}
