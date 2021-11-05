package com.jjak0b.android.trackingmypantry.data.model.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import java.util.Date;
import java.util.Objects;

@Entity(
    tableName = "products"
)
public class Product {
    @PrimaryKey
    @Expose
    @NonNull
    @ColumnInfo( name = "id")
    private String id;

    @Expose
    @NonNull
    private String barcode;

    @ColumnInfo( name = "user_id")
    @Expose
    private String userId;

    @Expose
    private String name;

    @Expose
    private String description;

    @Expose
    // img url
    private String img;

    @Expose
    private Date createdAt;

    @Expose
    private Date updatedAt;

    public Product(@NonNull String id, @NonNull String barcode, @NonNull String name, @Nullable String description, @Nullable String img, @NonNull Date createdAt, @NonNull Date updatedAt ) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.description = description;
        this.img = img;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Ignore
    public Product(@NonNull String barcode, @NonNull String name, @Nullable String description ) {
        this.barcode = barcode;
        this.name = name;
        this.description = description;
    }

    @Ignore
    public Product(@NonNull String id, @NonNull String barcode, @NonNull String name, @Nullable String description) {
        this( barcode, name, description );
        this.id = id;
    }

    @Ignore
    public Product(@NonNull String id, @NonNull String barcode, @NonNull String name, @Nullable String description, @Nullable String img ) {
        this( barcode, name, description );
        this.id = id;
        this.img = img;
    }

    public Product( @NonNull Product p) {
        this.id = p.id;
        this.barcode = p.barcode;
        this.userId = p.userId;
        this.name = p.name;
        this.description = p.description;
        this.img = p.img;
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

    public String getImg(){ return img; }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", barcode='" + barcode + '\'' +
                ", userID='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", img='" + img + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        else if( obj != null && obj instanceof Product ){
            Product o = (Product)obj;
            return Objects.equals( id, o.id)
            && Objects.equals( id, o.id)
            && Objects.equals( barcode, o.barcode)
            && Objects.equals( userId, o.userId)
            && Objects.equals( name, o.name)
            && Objects.equals( description, o.description)
            && Objects.equals( img, o.img);
        }
        return super.equals( obj );
    }

    public static class Builder {

        private String name;
        private String description;
        private String barcode;
        private String productId;
        private String userId;
        private String img;

        public Builder from( Product p ) {
            if( p != null ) {
                setName(p.getName());
                setDescription(p.getDescription());
                setBarcode(p.getBarcode());
                setProductId(p.getId());
                setImg( p.getImg() );
                setUserId(p.getUserId());
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

        public String getImg() {
            return img;
        }

        public String getUserId() {
            return userId;
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

        public Builder setImg(String img) {
            this.img = img;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Product build() {
            Product item = new Product( productId, barcode, name, description, img );
            item.setUserId(userId);
            return item;
        }
    }
}
