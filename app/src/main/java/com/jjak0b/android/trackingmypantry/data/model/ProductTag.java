package com.jjak0b.android.trackingmypantry.data.model;

public class ProductTag {
    int id;
    String name;

    public ProductTag(int id, String name ){
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
