package com.jjak0b.android.trackingmypantry.data.dataSource;

import com.jjak0b.android.trackingmypantry.data.HttpClient;

import com.jjak0b.android.trackingmypantry.data.model.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.model.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.model.RegisterCredentials;
import com.jjak0b.android.trackingmypantry.data.services.remote.RemoteAuthAPIService;


import org.jetbrains.annotations.NotNull;

import retrofit2.Callback;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private static LoginDataSource instance;
    private RemoteAuthAPIService service;

    public LoginDataSource() {
        service = HttpClient.getInstance()
                .create(RemoteAuthAPIService.class);
    }

    public static LoginDataSource getInstance() {
        if( instance == null ) {
            instance = new LoginDataSource();
        }

        return instance;
    }

    public void register(@NotNull RegisterCredentials credentials, Callback<RegisterCredentials> cb ) {
        service.createUser( credentials )
                .enqueue(cb);
    }

    public void login(@NotNull LoginCredentials credentials, Callback<AuthLoginResponse> cb ) {
        service.getAccessToken( credentials )
                .enqueue(cb);
    }
}