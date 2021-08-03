package com.jjak0b.android.trackingmypantry.data.model;

import android.location.Location;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

@Entity(
    foreignKeys = {
            @ForeignKey(
                    entity = Product.class,
                    parentColumns = "id",
                    childColumns = "product_id"
            ),
            @ForeignKey(
                    entity = Pantry.class,
                    parentColumns = "pantry_id",
                    childColumns = "pantry_id"
            )
    }
)
public class ProductInstanceGroup {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo( name = "id")
    long id;

    @NotNull
    @ColumnInfo( name = "product_id")
    String productId;

    @ColumnInfo( name = "pantry_id")
    long pantryId;

    @ColumnInfo( name = "quantity")
    int quantity;

    @Embedded(prefix = "purchaseInfo")
    PurchaseInfo purchaseInfo;

    // instance info
    Date expiryDate;
    float currentAmountPercent;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public long getPantryId() {
        return pantryId;
    }

    public void setPantryId(long pantryId) {
        this.pantryId = pantryId;
    }

    public PurchaseInfo getPurchaseInfo() {
        return purchaseInfo;
    }

    public void setPurchaseInfo(PurchaseInfo purchaseInfo) {
        this.purchaseInfo = purchaseInfo;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public float getCurrentAmountPercent() {
        return currentAmountPercent;
    }

    public void setCurrentAmountPercent(float currentAmountPercent) {
        this.currentAmountPercent = currentAmountPercent;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
