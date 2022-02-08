package com.jjak0b.android.trackingmypantry.data.dataSource;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.jjak0b.android.trackingmypantry.data.HttpClient;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.AuthException;
import com.jjak0b.android.trackingmypantry.data.api.HttpErrorApiResponseHandler;
import com.jjak0b.android.trackingmypantry.data.auth.AuthResultState;
import com.jjak0b.android.trackingmypantry.data.db.entities.User;
import com.jjak0b.android.trackingmypantry.data.services.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.services.API.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.services.API.RegisterCredentials;
import com.jjak0b.android.trackingmypantry.data.services.RemoteAuthAPIService;

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

    public LiveData<ApiResponse<User>> register(@NonNull RegisterCredentials credentials) {
        MediatorLiveData<ApiResponse<User>> mediator = new MediatorLiveData<>();
        LiveData<ApiResponse<User>> mRealLogin = service.createUser( credentials );

        // Add a mediator to specify (if any) why we got an unauthorized error
        mediator.addSource( mRealLogin, response -> {
            mediator.removeSource(mRealLogin);

            if( !HttpErrorApiResponseHandler.handle(response, 401, httpError -> {
                // credentials must be wrong
                mediator.postValue(ApiResponse.create(new AuthException(httpError, AuthResultState.FAILED)));
                return true;
            })) {
                mediator.postValue(response);
            }
        });

        return mediator;
    }

    public LiveData<ApiResponse<AuthLoginResponse>> login(@NonNull LoginCredentials credentials ) {
        MediatorLiveData<ApiResponse<AuthLoginResponse>> mediator = new MediatorLiveData<>();
        LiveData<ApiResponse<AuthLoginResponse>> mRealLogin = service.getAccessToken( credentials );

        // Add a mediator to specify (if any) why we got an unauthorized error
        mediator.addSource( mRealLogin, response -> {
            mediator.removeSource(mRealLogin);

            if( !HttpErrorApiResponseHandler.handle(response, 401, httpError -> {
                // credentials must be wrong
                mediator.postValue(ApiResponse.create(new AuthException(httpError, AuthResultState.FAILED)));
                return true;
            })) {
                mediator.postValue(response);
            }
        });

        return mediator ;
    }

    public LiveData<ApiResponse<User>> whoAmI(@NonNull String accessToken){
        StringBuilder authorization = new StringBuilder()
                .append( "Bearer ")
                .append( accessToken );

        MediatorLiveData<ApiResponse<User>> mediator = new MediatorLiveData<>();
        LiveData<ApiResponse<User>> mRealAPI = service.whoAmI(authorization.toString());

        // Add a mediator to specify (if any) why we got an unauthorized error
        mediator.addSource( mRealAPI, response -> {
            mediator.removeSource(mRealAPI);

            if( !HttpErrorApiResponseHandler.handle(response, 401, httpError -> {
                // credentials must be wrong
                mediator.postValue(ApiResponse.create(new AuthException(httpError, AuthResultState.UNAUTHORIZED)));
                return true;
            })) {
                mediator.postValue(response);
            }
        });
        return mediator;
    }
}