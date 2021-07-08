package com.jjak0b.android.trackingmypantry.data.services.remote;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.model.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.model.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.model.RegisterCredentials;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.adapter.guava.GuavaCallAdapterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RemoteAuthAPIService {

    /**
     * @see GuavaCallAdapterFactory provided exception and results in callback
     */
    @POST("/auth/login")
    ListenableFuture<AuthLoginResponse> getAccessToken(
            @Body LoginCredentials credentials
    );

    /**
     * @see GuavaCallAdapterFactory provided exception and results in callback
     */
    @POST("users")
    ListenableFuture<RegisterCredentials> createUser(
            @Body RegisterCredentials credentials
    );
}
