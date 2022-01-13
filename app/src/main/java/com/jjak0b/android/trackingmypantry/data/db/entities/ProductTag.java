package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
        tableName = "productTags",
        indices = {@Index(value = {"name"}, unique = true)}
)
public class ProductTag {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    long id;

    @NonNull
    String name;

    public static ProductTag creteDummy(String name){
        return new ProductTag(0, name);
    }

    public static boolean isDummy( ProductTag t ) {
        return t.id <= 0;
    }

    public ProductTag(long id, String name ){
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductTag that = (ProductTag) o;

        if( isDummy(this) || isDummy(that) ) {
            return Objects.equals(name.toLowerCase(), that.name.toLowerCase());
        }
        else {
            return id == that.id &&
                Objects.equals(name, that.name);
        }
    }
}
