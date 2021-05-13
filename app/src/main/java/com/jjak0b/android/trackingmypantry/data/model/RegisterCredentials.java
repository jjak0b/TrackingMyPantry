package com.jjak0b.android.trackingmypantry.data.model;

import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.NotNull;

public class RegisterCredentials extends LoginCredentials {

    @Expose(serialize = true, deserialize = true)
    private String username;

    public RegisterCredentials(@NotNull String username, @NotNull String email, @NotNull String password ) {
        super(email, password);
        this.username = username;
    }

    public String getUsername() { return username; }
}
