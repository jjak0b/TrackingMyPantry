package com.jjak0b.android.trackingmypantry.data.services.API;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;

public class CreateProduct extends UserProduct {

    @Expose
    String token;

    @Expose
    boolean test;

    public CreateProduct(@NonNull UserProduct product, @NonNull String token) {
        super( product );

        this.test = true; // TODO: remove on release
        this.token = token;
    }
}
