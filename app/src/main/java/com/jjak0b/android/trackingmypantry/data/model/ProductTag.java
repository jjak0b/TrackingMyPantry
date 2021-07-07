package com.jjak0b.android.trackingmypantry.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
    tableName = "productTags"
)
public class ProductTag {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    long id;
    String name;

    @Ignore
    public ProductTag( String name ){
        this.name = name;
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
        return id == that.id &&
                Objects.equals(name, that.name);
    }
}
