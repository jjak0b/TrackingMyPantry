package com.jjak0b.android.trackingmypantry.data.model;

import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.NotNull;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoginCredentials {

    @Expose(serialize = true, deserialize = true)
    private String email;

    @Expose(serialize = true, deserialize = false)
    private String password;

    @Expose(serialize = false, deserialize = true)
    private String accessToken;

    public LoginCredentials(@NotNull LoginCredentials credentials, @NotNull String accessToken ) {
        this.email = credentials.email;
        this.password = credentials.password;
        this.accessToken = accessToken;
    }

    public LoginCredentials(@NotNull String email, @NotNull String password) {
        this.email = email;
        this.password = password;
    }

    public LoginCredentials(@NotNull String email, @NotNull String password, @NotNull String accessToken ) {
        this.email = email;
        this.password = password;
        this.accessToken = accessToken;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }


    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return "LoginCredentials{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}