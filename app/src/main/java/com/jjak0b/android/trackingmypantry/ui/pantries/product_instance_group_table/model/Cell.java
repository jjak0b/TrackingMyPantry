package com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.model;


import org.jetbrains.annotations.Nullable;

public class Cell {
    @Nullable
    private Object mData;

    public Cell(@Nullable Object data) {
        this.mData = data;
    }

    @Nullable
    public Object getData() {
        return mData;
    }
}