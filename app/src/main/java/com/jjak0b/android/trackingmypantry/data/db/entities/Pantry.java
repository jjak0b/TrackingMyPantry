package com.jjak0b.android.trackingmypantry.data.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
        tableName = "pantries",
        indices = {
                // @Index(value = {"pantry_id", "owner_id"}, unique = true),
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
public class Pantry {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pantry_id", index = true)
    long id;

    @NonNull
    @ColumnInfo(name = "owner_id", index = true)
    String userId;

    @NonNull
    @ColumnInfo(name = "name")
    String name;


    @NonNull
    @Override
    public Pantry clone()  {
        return new Pantry( id, name, userId);
    }

    public static Pantry creteDummy(long id){
        return new Pantry(id, null,null);
    }
    public static Pantry creteDummy(String name){
        return new Pantry(0, name, null);
    }

    public static boolean isDummy( Pantry p ) {
        return p.id <= 0 || p.name == null;
    }

    public Pantry( long id, @NonNull String name, String userId){
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

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pantry pantry = (Pantry) o;
        return Objects.equals(id, pantry.id)
                && Objects.equals(userId, pantry.userId)
                && Objects.equals(name, pantry.name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
