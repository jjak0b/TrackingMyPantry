package com.jjak0b.android.trackingmypantry.data.services;

import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.db.entities.User;
import com.jjak0b.android.trackingmypantry.data.services.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.services.API.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.services.API.RegisterCredentials;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RemoteAuthAPIService {

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
    LiveData<ApiResponse<User>> _createUser(
            @Body RegisterCredentials credentials
    );

    @GET("users/me")
    LiveData<ApiResponse<User>> _whoAmI(
            @Header("Authorization") String authorization
    );
}
