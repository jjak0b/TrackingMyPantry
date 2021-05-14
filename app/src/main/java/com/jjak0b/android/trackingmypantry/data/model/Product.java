package com.jjak0b.android.trackingmypantry.data.model;

import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Product {
    @Expose
    String id;

    @Expose
    String barcode;

    @Expose
    String name;

    @Expose
    String description;

    public Product(@NotNull String barcode, @NotNull String name, @Nullable String description ) {
        this.barcode = barcode;
        this.name = name;
        this.description = description;
    }

    public Product(@NotNull String id, @NotNull String barcode, @NotNull String name, @Nullable String description) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.description = description;
    }

    public Product( @NotNull Product p) {
        this.id = p.id;
        this.barcode = p.barcode;
        this.name = p.name;
        this.description = p.description;
    }

    public String getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
