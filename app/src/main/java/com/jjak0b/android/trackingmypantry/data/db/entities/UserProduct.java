package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Objects;

@Entity(
        tableName = "userProducts",
        primaryKeys = { "product_id", "owner_id" },
        indices = {
                // each owner, can only have 1 unique product info
                @Index(value = { "product_id", "owner_id", "remote_id" }, unique = true )
        },
        foreignKeys = {
                @ForeignKey(
                        entity = ProductShared.class,
                        parentColumns = "id",
                        childColumns = "product_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "owner_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class UserProduct {

    /**
     * product remote id.
     * If null then this product hasn't been fetched on remote
     */
    @ColumnInfo( name = "remote_id")
    @SerializedName("id")
    @Expose
    @Nullable
    private String remote_id;

    @ColumnInfo( name = "product_id")
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

    @ColumnInfo( name = "owner_id" )
    @Expose(deserialize = false, serialize = false)
    @NonNull
    private String userOwnerId;

    @ColumnInfo(name = "p_name")
    @SerializedName("name")
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

    public UserProduct(@Nullable String remote_id, @NonNull String barcode, @NonNull String name, @Nullable String description, @Nullable String img, @Nullable String userCreatorId, @Nullable Date createdAt, @Nullable Date updatedAt, @NonNull String userOwnerId) {
        this.remote_id = remote_id;
        this.barcode = barcode;
        this.name = name;
        this.description = description;
        this.img = img;
        this.userCreatorId = userCreatorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userOwnerId = userOwnerId;
    }

    @Ignore
    public UserProduct(@NonNull String barcode, @NonNull String name, @Nullable String description ) {
        this(null, barcode, name, description, null, null, null, null, null);
    }

    @Ignore
    public UserProduct(@NonNull String remote_id, @NonNull String barcode, @NonNull String name, @Nullable String description) {
        this( remote_id, barcode, name, description, null, null, null, null, null);
    }

    @Ignore
    public UserProduct(@NonNull String remote_id, @NonNull String barcode, @NonNull String name, @Nullable String description, @Nullable String img ) {
        this( remote_id, barcode, name, description, img, null, null, null, null);
    }

    @Ignore
    public UserProduct(@NonNull UserProduct p) {
        this(p.remote_id, p.barcode, p.name, p.description, p.img, p.userCreatorId, p.createdAt, p.updatedAt, p.userOwnerId);
    }

    @Ignore
    public UserProduct() {
        this(null, null, null);
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

    public String getUserOwnerId() {
        return userOwnerId;
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

    public void setUserOwnerId(String userOwnerId) {
        this.userOwnerId = userOwnerId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "remote_id='" + remote_id + '\'' +
                ", barcode='" + barcode + '\'' +
                ", userCreatorId='" + userCreatorId + '\'' +
                ", userOwnerId='" + userOwnerId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", img='" + (String.valueOf(img).length() < 20 ? img : String.valueOf(img).substring(0, 20)  ) + '\'' +
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
                && Objects.equals(userOwnerId, o.userOwnerId)
                && Objects.equals(name,o.name)
                && Objects.equals(description, o.description)
                && Objects.equals(img, o.img)
                && Objects.equals(createdAt, o.createdAt)
                && Objects.equals(updatedAt, o.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remote_id, barcode, userCreatorId, userOwnerId, name, description, img, createdAt, updatedAt);
    }
}
