package com.jjak0b.android.trackingmypantry.data.model;

import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

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

    private Date tokenExpireDate;

    public LoginCredentials(@NotNull LoginCredentials credentials, @NotNull String accessToken ) {
        this( credentials.email, credentials.password, accessToken );
    }

    public LoginCredentials(@NotNull String email, @NotNull String password) {
        this.email = email;
        this.password = password;
    }

    public LoginCredentials(@NotNull String email, @NotNull String password, @NotNull String accessToken ) {
        this( email, password );
        this.accessToken = accessToken;
    }

    public LoginCredentials(@NotNull LoginCredentials credentials, @NotNull String accessToken, @NotNull Date tokenExpireDate) {
        this( credentials, accessToken );
        this.tokenExpireDate = tokenExpireDate;
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

    public Date getTokenExpireDate() {
        return tokenExpireDate;
    }

    public boolean isAccessTokenExpired() {
        return tokenExpireDate == null || tokenExpireDate.before( Calendar.getInstance().getTime() );
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