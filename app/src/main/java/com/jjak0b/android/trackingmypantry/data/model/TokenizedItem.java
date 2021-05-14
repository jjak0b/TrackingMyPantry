package com.jjak0b.android.trackingmypantry.data.model;

import com.google.gson.annotations.Expose;

public class TokenizedItem {
    @Expose
    String token;

    public TokenizedItem( String token ) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
