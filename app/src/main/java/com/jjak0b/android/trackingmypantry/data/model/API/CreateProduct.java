package com.jjak0b.android.trackingmypantry.data.model.API;

import com.google.gson.annotations.Expose;
import com.jjak0b.android.trackingmypantry.data.model.Product;

import org.jetbrains.annotations.NotNull;

public class CreateProduct extends Product {

    @Expose
    String token;

    @Expose
    boolean test;

    public CreateProduct( @NotNull Product product, @NotNull String token) {
        super( product );

        this.test = false;
        this.token = token;
    }
}
