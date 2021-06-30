package com.jjak0b.android.trackingmypantry.data;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.GsonBuilder;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.auth.AuthResultState;
import com.jjak0b.android.trackingmypantry.data.dataSource.LoginDataSource;
import com.jjak0b.android.trackingmypantry.data.model.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.model.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.model.RegisterCredentials;


import java.util.Calendar;
import java9.util.concurrent.CompletableFuture;
import java9.util.function.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static final String TAG = "LoginRepository";

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;


    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private MutableLiveData<LoginCredentials> mLoggedInUser;
    private LiveEvent<AuthResultState> mAuthResultState;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance() {
        return getInstance( LoginDataSource.getInstance() );
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
            instance.mLoggedInUser = new MutableLiveData<LoginCredentials>();
        }

        return instance;
    }

    public boolean isLoggedIn() {
        return mLoggedInUser.getValue() != null && mLoggedInUser.getValue().getAccessToken() != null;
    }

    /**
     * Provide the access token based on current credentials of the logged in user.
     * If an existing access token has been expired then will sign in again renewing the access token and provide the new one;
     * the access token will be provided in Result.Success instance.
     * Otherwise if there is no logged in user or any error happen during sign in operation will be reported as Result.Error instance
     * @return a completable with the access Token operation result
     */
    public CompletableFuture<Result<String, AuthResultState>> requireAuthorization() {

        CompletableFuture<Result<String, AuthResultState>> futureAuthorization = new CompletableFuture<>();

        StringBuilder authBuilder = new StringBuilder()
                .append( "Bearer ");
        if( isLoggedIn() ){
            LoginCredentials credentials = getLoggedInUser().getValue();
            if( credentials.isAccessTokenExpired() ){
                signIn(credentials)
                        .thenAccept(new Consumer<Result<AuthResultState, AuthResultState>>() {
                            @Override
                            public void accept(Result<AuthResultState, AuthResultState> result) {
                                if( result instanceof Result.Success ) {
                                    authBuilder.append( getLoggedInUser().getValue().getAccessToken() );
                                    futureAuthorization.complete( new Result.Success<>( authBuilder.toString() ) );
                                }
                                else {
                                    Result.Error<AuthResultState, AuthResultState> error
                                            = (Result.Error<AuthResultState, AuthResultState>) result;
                                    futureAuthorization.complete( new Result.Error<>(error.getError()) );
                                }
                            }
                        });
            }
            else {
                authBuilder.append( getLoggedInUser().getValue().getAccessToken() );
                futureAuthorization.complete( new Result.Success<>( authBuilder.toString() ) );
            }
        }
        else {
            futureAuthorization.complete( new Result.Error<>( AuthResultState.UNAUTHORIZED ) );
        }

        return futureAuthorization;
    }

    public MutableLiveData<LoginCredentials> getLoggedInUser() {
        return this.mLoggedInUser;
    }

    public void setLoggedInUser(LoginCredentials user) {
        if( user == null ) {
            logout();
        }
        else {
            this.mLoggedInUser.setValue( user );
            // If user credentials will be cached in local storage, it is recommended it be encrypted
            // @see https://developer.android.com/training/articles/keystore
        }
    }

    public void logout() {
        mLoggedInUser.setValue( null );
    }

    public CompletableFuture<Result<AuthResultState, AuthResultState>> signIn(LoginCredentials credentials) {

        CompletableFuture<Result<AuthResultState, AuthResultState>> future = new CompletableFuture<>();

        dataSource.login(credentials, new Callback<AuthLoginResponse>() {
            @Override
            public void onResponse(Call<AuthLoginResponse> call, Response<AuthLoginResponse> response) {
                if( response.isSuccessful() ) {
                    if( response.body() != null ) {
                        Log.d(TAG, "Login Success: " + response.toString() );

                        // set expire date pf the access token after 7 days from now
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, 7 );

                        setLoggedInUser( new LoginCredentials( credentials, response.body().getAccessToken(), cal.getTime() ) );
                        future.complete( new Result.Success<>( AuthResultState.AUTHORIZED ) );
                    }
                    else {
                        Log.e(TAG, "Login body parsing Failed: " + response.toString() );
                        future.complete(  new Result.Error<>( AuthResultState.FAILED ) );
                    }
                }
                else {
                    Log.e( TAG, "Login Failed, reason: " + response.toString() );
                    future.complete( new Result.Error<>( AuthResultState.UNAUTHORIZED ) );
                }
            }

            @Override
            public void onFailure(Call<AuthLoginResponse> call, Throwable t) {
                Log.e( TAG, "Login try Failed, reason: " + t );
                future.complete( new Result.Error<>( AuthResultState.FAILED ) );
            }
        });

        return future;
    }

    public CompletableFuture<Result<AuthResultState, AuthResultState>> signUp(RegisterCredentials credentials ) {
        CompletableFuture<Result<AuthResultState, AuthResultState>> future = new CompletableFuture<>();

        dataSource.register(credentials, new Callback<RegisterCredentials>() {
            @Override
            public void onResponse(Call<RegisterCredentials> call, Response<RegisterCredentials> response) {
                if( response.isSuccessful() ) {
                    Log.d(TAG, "Register Success: " + response.toString()  );
                    future.complete( new Result.Success<>( AuthResultState.AUTHORIZED ) );
                }
                else {
                    Log.e( TAG, "Register Failed, reason: " + response.toString() );
                    future.complete( new Result.Error<>( AuthResultState.UNAUTHORIZED ) );
                }
            }

            @Override
            public void onFailure(Call<RegisterCredentials> call, Throwable t) {
                Log.e( TAG, "Login try Failed, reason: " + t );
                future.complete( new Result.Error<>( AuthResultState.FAILED ) );
            }
        });

        return future;
    }
}