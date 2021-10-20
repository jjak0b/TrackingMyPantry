package com.jjak0b.android.trackingmypantry.data.auth;

import androidx.annotation.NonNull;

public class AuthException extends Exception {

    private AuthResultState state;
    public AuthException(@NonNull AuthResultState state) {
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
