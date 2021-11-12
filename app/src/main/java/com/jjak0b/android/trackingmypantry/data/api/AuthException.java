package com.jjak0b.android.trackingmypantry.data.api;

import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.data.auth.AuthResultState;

public class AuthException extends RemoteException {

    private AuthResultState state;
    public AuthException(@NonNull AuthResultState state) {
        super();
        this.state = state;
    }

    public AuthException(Throwable cause, @NonNull AuthResultState state ) {
        super(cause);
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
