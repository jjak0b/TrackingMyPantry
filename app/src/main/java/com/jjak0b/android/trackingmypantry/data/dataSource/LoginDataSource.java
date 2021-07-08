package com.jjak0b.android.trackingmypantry.data.dataSource;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.HttpClient;

import com.jjak0b.android.trackingmypantry.data.model.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.model.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.model.RegisterCredentials;
import com.jjak0b.android.trackingmypantry.data.services.remote.RemoteAuthAPIService;


import org.jetbrains.annotations.NotNull;

import retrofit2.adapter.guava.GuavaCallAdapterFactory;

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

    /**
     * @see GuavaCallAdapterFactory provided exception and results in callback
     * @param credentials
     * @return
     */
    public ListenableFuture<RegisterCredentials> register(@NotNull RegisterCredentials credentials) {
        return service.createUser( credentials );
    }

    /**
     * @see GuavaCallAdapterFactory provided exception and results in callback
     * @param credentials
     * @return
     */
    public ListenableFuture<AuthLoginResponse> login(@NotNull LoginCredentials credentials ) {
        return service.getAccessToken( credentials );
    }
}