package com.jjak0b.android.trackingmypantry.data.model.services.API;

import com.google.gson.annotations.Expose;

public class AuthLoginResponse {

    @Expose
    String accessToken;

    public String getAccessToken() {
        return accessToken;
    }
}
