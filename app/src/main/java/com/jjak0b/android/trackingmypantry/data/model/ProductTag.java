package com.jjak0b.android.trackingmypantry.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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

    @Override
    public String toString() {
        return getName();
    }
}
