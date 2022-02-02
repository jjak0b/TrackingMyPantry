package com.jjak0b.android.trackingmypantry.data.db.entities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Objects;

@Entity(
        tableName = "purchaseInfo",
        foreignKeys = {
                @ForeignKey(
                        entity = ProductShared.class,
                        parentColumns = "id",
                        childColumns = "product_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Place.class,
                        parentColumns = "id",
                        childColumns = "place_id",
                        onDelete = ForeignKey.SET_DEFAULT
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class PurchaseInfo {
    @ColumnInfo(name = "id" )
    @PrimaryKey(autoGenerate = true)
    long id;

    @ColumnInfo( defaultValue = "CURRENT_TIMESTAMP" )
    Date purchaseDate;

    @ColumnInfo( defaultValue = "0" )
    float cost;

    @NonNull
    @ColumnInfo( name = "product_id", index = true)
    String productId;

    @ColumnInfo( name = "place_id", index = true, defaultValue = "NULL")
    @Nullable
    String placeId;

    @NonNull
    @ColumnInfo( name = "user_id", index = true)
    String userId;

    public PurchaseInfo(@NonNull String productId, float cost, Date purchaseDate, @Nullable String placeId, @NonNull String userId ) {
        this.id = 0;
        this.purchaseDate = purchaseDate;
        this.productId = productId;
        this.placeId = placeId;
        this.userId = userId;
        this.cost = cost;
    }

    @Ignore
    public PurchaseInfo(float cost, Date purchaseDate, @Nullable String placeId) {
        this( null, cost, purchaseDate, placeId, null);
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

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseInfo that = (PurchaseInfo) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(placeId, that.placeId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(purchaseDate, that.purchaseDate) &&
                Float.compare(cost, that.cost) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, purchaseDate, cost, productId, placeId, userId);
    }

    @Override
    public String toString() {
        return "PurchaseInfo{" +
                "id=" + id +
                ", purchaseDate=" + purchaseDate +
                ", cost=" + cost +
                ", productId='" + productId + '\'' +
                ", placeId='" + placeId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
