package com.jjak0b.android.trackingmypantry.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;


@Entity(
        tableName = "users"
)
public class User {
    @PrimaryKey
    @Expose
    @NonNull
    @ColumnInfo( name = "id")
    private String id;

    @NonNull
    @Expose
    private String username;

    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public User(User user) {
        this( user.id, user.username );
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
