package com.jjak0b.android.trackingmypantry.data.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Objects;

@Entity(
        tableName = "purchaseInfo",
        foreignKeys = {
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "id",
                        childColumns = "product_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Place.class,
                        parentColumns = "id",
                        childColumns = "place_id",
                        onDelete = ForeignKey.SET_DEFAULT
                )
        }
)
public class PurchaseInfo {
    @PrimaryKey(autoGenerate = true)
    long id;

    Date purchaseDate;

    @ColumnInfo( defaultValue = "0" )
    float cost;

    @NonNull
    @ColumnInfo( name = "product_id")
    String productId;

    @ColumnInfo( name = "place_id", defaultValue = "NULL")
    @Nullable
    String placeId;

    public PurchaseInfo(@NonNull String productId, float cost, Date purchaseDate, @Nullable String placeId) {
        this.id = 0;
        this.purchaseDate = purchaseDate;
        this.productId = productId;
        this.placeId = placeId;
        this.cost = cost;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }


    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @NonNull
    public String getProductId() {
        return productId;
    }

    public void setProductId(@NonNull String productId) {
        this.productId = productId;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseInfo that = (PurchaseInfo) o;
        return Objects.equals(id, id) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(placeId, that.placeId) &&
                Objects.equals(purchaseDate, that.purchaseDate) &&
                Float.compare(that.cost, cost) == 0;
    }

}
