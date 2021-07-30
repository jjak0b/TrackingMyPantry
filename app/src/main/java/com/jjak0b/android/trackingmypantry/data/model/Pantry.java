package com.jjak0b.android.trackingmypantry.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
        tableName = "pantries",
        indices = {@Index(value = {"name"}, unique = true)}
)
public class Pantry {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pantry_id")
    long id;

    String name;


    @NonNull
    @Override
    public Pantry clone()  {
        return new Pantry( id, name );
    }

    public static Pantry creteDummy(long id){
        return new Pantry(id, null);
    }
    public static Pantry creteDummy(String name){
        return new Pantry(0, name);
    }

    public static boolean isDummy( Pantry p ) {
        return p.id <= 0 || p.name == null;
    }

    public Pantry( long id, String name ){
        this.id = id;
        this.name = name;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pantry pantry = (Pantry) o;
        return id == pantry.id;
    }
}
