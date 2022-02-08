package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;

@Entity(
        tableName = "products"
)
public class ProductShared {
    @ColumnInfo( name = "id", index = true)
    @PrimaryKey
    @Expose
    @NonNull
    private String barcode;

    public ProductShared(@NonNull String barcode) {
        this.barcode = barcode;
    }

    @NonNull
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(@NonNull String barcode) {
        this.barcode = barcode;
    }
}
