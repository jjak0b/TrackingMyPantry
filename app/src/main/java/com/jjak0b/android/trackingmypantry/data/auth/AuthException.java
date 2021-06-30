package com.jjak0b.android.trackingmypantry.data.auth;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class AuthException extends Exception {

    private AuthResultState state;
    public AuthException(@NotNull AuthResultState state) {
        super();
        this.state = state;
    }

    public AuthResultState getState(){
        return state;
    }

    @Override
    public String getMessage() {
        return getState().toString();
    }
}
