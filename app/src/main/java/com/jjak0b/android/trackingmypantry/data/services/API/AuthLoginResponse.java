package com.jjak0b.android.trackingmypantry.data.services.API;

import com.google.gson.annotations.Expose;

public class AuthLoginResponse {

    @Expose
    String accessToken;

    public String getAccessToken() {
        return accessToken;
    }
}
