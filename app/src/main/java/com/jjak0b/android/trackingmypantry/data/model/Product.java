package com.jjak0b.android.trackingmypantry.data.model;

import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Product {
    @Expose
    private String id;

    @Expose
    private String barcode;

    @Expose
    private String name;

    @Expose
    private String description;

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

    public static class Builder {

        private String name;
        private String description;
        private String barcode;
        private String productId;

        public Builder from( Product p ) {
            if( p != null ) {
                setName(p.getName());
                setDescription(p.getDescription());
                setBarcode(p.getBarcode());
                setProductId(p.getId());
            }
            return this;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getBarcode() {
            return barcode;
        }

        public String getProductId() {
            return productId;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setProductId(String id) {
            this.productId = id;
            return this;
        }

        public Builder setBarcode(String barcode) {
            this.barcode =  barcode;
            return this;
        }

        public Product build() {
            Product item = new Product( productId, barcode, name, description );
            return item;
        }
    }
}
