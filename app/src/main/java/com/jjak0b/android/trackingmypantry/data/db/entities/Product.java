package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Objects;

@Entity(
        tableName = "products",
        indices = {
                @Index(value = {"id"}, unique = true),
                @Index(value = {"remote_id"}, unique = true)
        }
)
public class Product {
    /**
     * product remote id.
     * If null then this product hasn't been fetched on remote
     */
    @ColumnInfo( name = "remote_id")
    @SerializedName("id")
    @Expose
    @Nullable
    private String remote_id;

    @PrimaryKey
    @ColumnInfo( name = "id")
    @Expose
    @NonNull
    private String barcode;

    /**
     * The product creator is the remote user id of a remote user who created this product template on remote
     */
    @ColumnInfo( name = "creator_id")
    @SerializedName("userId")
    @Expose
    @Nullable
    private String userCreatorId;

    @Expose
    @NonNull
    private String name;

    @Expose
    @Nullable
    private String description;

    @Expose
    @Nullable
    // img url
    private String img;

    @Expose
    @Nullable
    private Date createdAt;

    @Expose
    @Nullable
    private Date updatedAt;

    public Product(@Nullable String remote_id, @NonNull String barcode, @NonNull String name, @Nullable String description, @Nullable String img, @Nullable String userCreatorId, @Nullable Date createdAt, @Nullable Date updatedAt ) {
        this.remote_id = remote_id;
        this.barcode = barcode;
        this.name = name;
        this.description = description;
        this.img = img;
        this.userCreatorId = userCreatorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Ignore
    public Product(@NonNull String barcode, @NonNull String name, @Nullable String description ) {
        this(null, barcode, name, description, null, null, null, null);
    }

    @Ignore
    public Product(@NonNull String remote_id, @NonNull String barcode, @NonNull String name, @Nullable String description) {
        this( remote_id, barcode, name, description, null, null, null, null );
    }

    @Ignore
    public Product(@NonNull String remote_id, @NonNull String barcode, @NonNull String name, @Nullable String description, @Nullable String img ) {
        this( remote_id, barcode, name, description, img, null, null, null );
    }

    @Ignore
    public Product( @NonNull Product p) {
        this(p.remote_id, p.barcode, p.name, p.description, p.img, p.userCreatorId, p.createdAt, p.updatedAt );
    }

    public String getRemote_id() {
        return remote_id;
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

    public String getUserCreatorId() {
        return userCreatorId;
    }

    public void setRemote_id(String remote_id) {
        this.remote_id = remote_id;
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

    public void setUserCreatorId(String userCreatorId){
        this.userCreatorId = userCreatorId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + remote_id + '\'' +
                ", barcode='" + barcode + '\'' +
                ", userID='" + userCreatorId + '\'' +
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
            return Objects.equals(remote_id, o.remote_id)
            && Objects.equals(remote_id, o.remote_id)
            && Objects.equals( barcode, o.barcode)
            && Objects.equals(userCreatorId, o.userCreatorId)
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
                setProductId(p.getRemote_id());
                setImg( p.getImg() );
                setUserId(p.getUserCreatorId());
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
            item.setUserCreatorId(userId);
            return item;
        }
    }
}
