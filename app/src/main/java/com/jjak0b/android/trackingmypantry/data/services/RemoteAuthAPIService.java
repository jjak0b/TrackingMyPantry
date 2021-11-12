package com.jjak0b.android.trackingmypantry.data.services;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.services.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.services.API.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.services.API.RegisterCredentials;
import com.jjak0b.android.trackingmypantry.data.db.entities.User;

import retrofit2.adapter.guava.GuavaCallAdapterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
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

    /**
     * @see GuavaCallAdapterFactory provided exception and results in callback
     * @param authorization
     * @return
     */
    @GET("users/me")
    ListenableFuture<User> whoAmI(
            @Header("Authorization") String authorization
    );

    /**
     * @see LiveDataCallAdapterFactory
     */
    @POST("/auth/login")
    LiveData<ApiResponse<AuthLoginResponse>> _getAccessToken(
            @Body LoginCredentials credentials
    );

    /**
     * @see LiveDataCallAdapterFactory
     */
    @POST("users")
    LiveData<ApiResponse<RegisterCredentials>> _createUser(
            @Body RegisterCredentials credentials
    );

    @GET("users/me")
    LiveData<ApiResponse<User>> _whoAmI(
            @Header("Authorization") String authorization
    );
}
