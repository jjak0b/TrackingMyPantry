package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import com.google.gson.annotations.Expose;

import java.util.Date;
import java.util.Objects;

@Entity(
        tableName = "userProducts",
        primaryKeys = { "product_id", "owner_id" },
        indices = {
                @Index(value = { "product_id", "owner_id" }, unique = true ),
                // each owner, can only have 1 unique remote product
                @Index(value = { "owner_id", "remote_id" }, unique = true )
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
public class UserProduct extends Product {

    @ColumnInfo( name = "owner_id" )
    @Expose(deserialize = false, serialize = false)
    @NonNull
    private String userOwnerId;

    public UserProduct(@Nullable String remote_id, @NonNull String barcode, @NonNull String name, @Nullable String description, @Nullable String img, @Nullable String userCreatorId, @Nullable Date createdAt, @Nullable Date updatedAt, @NonNull String userOwnerId) {
        super(remote_id, barcode, userCreatorId, name, description, img, createdAt, updatedAt);
        this.userOwnerId = userOwnerId;
    }

    @Ignore
    public UserProduct(@NonNull Product p, @NonNull String userOwnerId ) {
        this(p.remote_id, p.barcode, p.name, p.description, p.img, p.userCreatorId, p.createdAt, p.updatedAt, userOwnerId);
    }

    @Ignore
    public UserProduct(@NonNull UserProduct p) {
        this(p, p.userOwnerId);
    }

    @Ignore
    public UserProduct() {
        this(null, null, null, null, null, null, null, null, null);
    }

    public String getUserOwnerId() {
        return userOwnerId;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProduct)) return false;
        UserProduct that = (UserProduct) o;
        return
                Objects.equals(userOwnerId, that.userOwnerId)
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remote_id, barcode, userCreatorId, userOwnerId, name, description, img, createdAt, updatedAt);
    }
}
