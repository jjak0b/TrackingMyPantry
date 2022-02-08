package com.jjak0b.android.trackingmypantry.data.db.results;

import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;

import java.util.Date;

public class ExpirationInfo {

    public long pantry_id;
    public String product_id;

    public String product_name;
    public String pantry_name;

    public Date expiryDate;
    public int quantity;

    public ExpirationInfo() {

    }

    @Override
    public String toString() {
        return "ExpirationInfo{" +
                "pantry=" + pantry_name +
                ", product=" + product_name +
                ", expireDate=" + expiryDate +
                ", quantity=" + quantity +
                '}';
    }

    public static class Dummy {
        long pantryID;
        String productID;
        Date expireDate;
        int quantity;

        public Dummy() {

        }

        public Dummy(@NonNull ProductInstanceGroup from) {
            setExpireDate(from.getExpiryDate());
            setPantryID(from.getPantryId());
            setProductID(from.getProductId());
            setQuantity(from.getQuantity());
        }

        public String getProductID() {
            return productID;
        }

        public Long getPantryID() {
            return pantryID;
        }

        public Date getExpireDate() {
            return expireDate;
        }

        public int getQuantity() {
            return quantity;
        }

        public Dummy setProductID(String productID) {
            this.productID = productID;
            return this;
        }

        public Dummy setPantryID(long pantryID) {
            this.pantryID = pantryID;
            return this;
        }

        public Dummy setExpireDate(Date expireDate) {
            this.expireDate = expireDate;
            return this;
        }

        public Dummy setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }
    }
}