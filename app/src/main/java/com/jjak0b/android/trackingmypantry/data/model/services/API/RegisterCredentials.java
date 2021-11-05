package com.jjak0b.android.trackingmypantry.data.model.services.API;

import com.google.gson.annotations.Expose;
import com.jjak0b.android.trackingmypantry.data.model.services.API.LoginCredentials;

import androidx.annotation.NonNull;

public class RegisterCredentials extends LoginCredentials {

    @Expose(serialize = true, deserialize = true)
    private String username;

    public RegisterCredentials(@NonNull String username, @NonNull String email, @NonNull String password ) {
        super(email, password);
        this.username = username;
    }

    public String getUsername() { return username; }
}
