package com.jjak0b.android.trackingmypantry.data.services.API;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;

public class CreateProduct extends Product {

    @Expose
    String token;

    @Expose
    boolean test;

    public CreateProduct(@NonNull Product product, @NonNull String token) {
        super( product );

        this.test = true; // TODO: remove on release
        this.token = token;
    }

    @Override
    public String toString() {
        return "CreateProduct{" +
                "token='" + token + '\'' +
                ", test=" + test +
                ", " + super.toString() +
                '}';
    }
}
