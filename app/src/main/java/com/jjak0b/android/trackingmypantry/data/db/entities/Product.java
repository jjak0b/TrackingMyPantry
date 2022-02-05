package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Objects;

public class Product {
    /**
     * product remote id.
     * If null then this product hasn't been fetched on remote
     */
    @ColumnInfo(name = "remote_id")
    @SerializedName("id")
    @Expose
    @Nullable
    protected String remote_id;

    @ColumnInfo(name = "product_id")
    @Expose
    @NonNull
    protected String barcode;
    /**
     * The product creator is the remote user id of a remote user who created this product template on remote
     */
    @ColumnInfo(name = "creator_id")
    @SerializedName("userId")
    @Expose(serialize = false)
    @Nullable
    protected String userCreatorId;

    @ColumnInfo(name = "p_name")
    @SerializedName("name")
    @Expose
    @NonNull
    protected String name;

    @Expose
    @Nullable
    protected String description;

    @Expose
    @Nullable
    // img url
    protected String img;

    @Expose
    @Nullable
    protected Date createdAt;

    @Expose
    @Nullable
    protected Date updatedAt;

    public Product(@Nullable String remote_id, @NonNull String barcode, @NonNull String name, @Nullable String description, @Nullable String img, @Nullable String userCreatorId, @Nullable Date createdAt, @Nullable Date updatedAt) {
        this.remote_id = remote_id;
        this.barcode = barcode;
        this.userCreatorId = userCreatorId;
        this.name = name;
        this.description = description;
        this.img = img;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Ignore
    public Product(@NonNull Product p) {
        this(p.remote_id, p.barcode, p.name, p.description, p.img, p.userCreatorId, p.createdAt, p.updatedAt);
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

    public String getImg() {
        return img;
    }

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

    public void setUserCreatorId(String userCreatorId) {
        this.userCreatorId = userCreatorId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "remote_id='" + remote_id + '\'' +
                ", barcode='" + barcode + '\'' +
                ", userCreatorId='" + userCreatorId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", img='" + (String.valueOf(img).length() < 32 ? img : (String.valueOf(img).substring(0, 32) + "..." )) + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserProduct o = (UserProduct) obj;
        return Objects.equals(remote_id, o.remote_id)
                && Objects.equals(barcode, o.barcode)
                && Objects.equals(userCreatorId, o.userCreatorId)
                && Objects.equals(name,o.name)
                && Objects.equals(description, o.description)
                && Objects.equals(img, o.img)
                && Objects.equals(createdAt, o.createdAt)
                && Objects.equals(updatedAt, o.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remote_id, barcode, userCreatorId, name, description, img, createdAt, updatedAt);
    }
}
