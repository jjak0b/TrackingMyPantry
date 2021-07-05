package com.jjak0b.android.trackingmypantry.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "pantries",
        indices = {@Index(value = {"name"}, unique = true)}
)
public class Pantry {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pantry_id")
    long id;

    String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
