package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Objects;

@Entity(
    indices = {
            @Index(value = {"product_id", "owner_id"} ),
            @Index(value = {"id","product_id"}, unique = true ),
            @Index(value = {"id","pantry_id"}, unique = true ),
            @Index(value = {"pantry_id", "product_id", "expiryDate", "currentAmountPercent", "quantity"}, unique = true )
    },
    foreignKeys = {
            @ForeignKey(
                    entity = UserProduct.class,
                    parentColumns = { "product_id", "owner_id" },
                    childColumns = {"product_id", "owner_id" },
                    onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE
            ),
            @ForeignKey(
                    entity = Pantry.class,
                    parentColumns = "pantry_id",
                    childColumns = "pantry_id",
                    onDelete = ForeignKey.CASCADE
            )
    }
)
public class ProductInstanceGroup {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo( name = "id")
    long id;

    @NonNull
    @ColumnInfo( name = "product_id")
    String productId;

    @ColumnInfo( name = "pantry_id")
    long pantryId;

    @ColumnInfo( name = "owner_id")
    @NonNull
    String userId;

    @ColumnInfo( defaultValue = "1" )
    int quantity;

    // instance info
    @ColumnInfo( defaultValue = "CURRENT_TIMESTAMP" )
    Date expiryDate;

    @IntRange(from = 0, to = 100)
    @ColumnInfo( defaultValue = "100" )
    int currentAmountPercent;

    public ProductInstanceGroup(){
        this.quantity = 1;
        this.currentAmountPercent = 100;
    }

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

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getCurrentAmountPercent() {
        return currentAmountPercent;
    }

    public void setCurrentAmountPercent(int currentAmountPercent) {
        this.currentAmountPercent = currentAmountPercent;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
        ProductInstanceGroup that = (ProductInstanceGroup) o;
        return  Objects.equals(id ,  that.id) &&
                Objects.equals(pantryId, that.pantryId) &&
                Objects.equals(productId, that.productId)  &&
                Objects.equals(userId, that.userId)  &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(currentAmountPercent, that.currentAmountPercent) &&
                Objects.equals(expiryDate, that.expiryDate);
    }

    @Override
    public String toString() {
        return "ProductInstanceGroup{" +
                "id=" + id +
                ", productId='" + productId + '\'' +
                ", pantryId=" + pantryId +
                ", userId=" + userId +
                ", quantity=" + quantity +
                ", expiryDate=" + expiryDate +
                ", currentAmountPercent=" + currentAmountPercent +
                '}';
    }

    public static ProductInstanceGroup from(ProductInstanceGroup o ){
        ProductInstanceGroup newO = new ProductInstanceGroup();
        newO.setId(o.getId());
        newO.setPantryId(o.getPantryId());
        newO.setProductId(o.getProductId());
        newO.setUserId(o.getUserId());
        newO.setQuantity(o.getQuantity());
        newO.setExpiryDate(o.getExpiryDate());
        newO.setCurrentAmountPercent(o.getCurrentAmountPercent());
        return newO;
    }
}
