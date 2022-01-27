package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
        tableName = "productTags",
        indices = {
                @Index(value = {"id", "owner_id"}, unique = true ),
                // each owner can have only 1 Pantry with same name
                @Index(value = {"owner_id", "name"}, unique = true)
        },
        foreignKeys = {
            @ForeignKey(
                    entity = User.class,
                    parentColumns = "id",
                    childColumns = "owner_id",
                    onDelete = ForeignKey.CASCADE
            )
        }
)
public class ProductTag {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    long id;

    @NonNull
    String name;

    @NonNull
    @ColumnInfo(name = "owner_id")
    String userId;

    public static ProductTag creteDummy(String name){
        return new ProductTag(0, name, null);
    }

    public static boolean isDummy( ProductTag t ) {
        return t.id <= 0;
    }

    public ProductTag(long id, @NonNull String name, String userId ){
        this.id = id;
        this.name = name;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        // Ensure won't return null
        return String.valueOf(getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductTag that = (ProductTag) o;

        if( isDummy(this) || isDummy(that) ) {
            return ( name != null && that.name != null ) && Objects.equals(name.toLowerCase(), that.name.toLowerCase());
        }
        else {
            return id == that.id &&
                Objects.equals(name, that.name)
                && Objects.equals(userId, that.userId);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, userId);
    }
}
