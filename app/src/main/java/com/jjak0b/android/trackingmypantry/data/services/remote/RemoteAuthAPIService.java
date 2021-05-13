package com.jjak0b.android.trackingmypantry.data.services.remote;

import com.jjak0b.android.trackingmypantry.data.model.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.model.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.model.RegisterCredentials;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RemoteAuthAPIService {

    @POST("/auth/login")
    Call<AuthLoginResponse> getAccessToken(
            @Body LoginCredentials credentials
    );

    @POST("users")
    Call<RegisterCredentials> createUser(
            @Body RegisterCredentials credentials
    );
}
