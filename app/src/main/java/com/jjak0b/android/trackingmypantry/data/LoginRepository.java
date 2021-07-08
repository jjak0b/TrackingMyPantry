package com.jjak0b.android.trackingmypantry.data;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.auth.AuthException;
import com.jjak0b.android.trackingmypantry.data.auth.AuthResultState;
import com.jjak0b.android.trackingmypantry.data.dataSource.LoginDataSource;
import com.jjak0b.android.trackingmypantry.data.model.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.model.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.model.RegisterCredentials;


import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import java.util.Calendar;
import java.util.concurrent.Executors;

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

    private static final int nTHREADS = 2;
    private static final ListeningExecutorService executor =
            MoreExecutors.listeningDecorator( Executors.newFixedThreadPool(nTHREADS) );

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

    private ListeningExecutorService getExecutor(){
        return executor;
    }

    public boolean isLoggedIn() {
        return mLoggedInUser.getValue() != null && mLoggedInUser.getValue().getAccessToken() != null;
    }

    /**
     * Provide the access token based on current credentials of the logged in user.
     * If an existing access token has been expired then will sign in again renewing the access token and provide the new one;
     * the access token will be provided as {@link ListenableFuture} result.
     * Otherwise if there is no logged in user or any error happen during sign in operation will be reported as {@link Exception}
     * if any error occurs then the following exceptions will be provided:
     * <ul>
     *     <li>{@link AuthException} if there are no auth credentials to request the access token</li>
     *     <li>{@link AuthException} if there are no auth credentials to request the access token</li>
     *     <li>{@link AuthException} if there are no auth credentials to request the access token</li>
     * </ul>
     * @return a future with the access Token
     */
    public ListenableFuture<String> requireAuthorization(boolean forceRefresh ) {

        StringBuilder authBuilder = new StringBuilder()
                .append( "Bearer ");
        if( isLoggedIn() ){
            LoginCredentials credentials = getLoggedInUser().getValue();
            if( forceRefresh || credentials.isAccessTokenExpired() ){
                return Futures.transform(
                        signIn(credentials),
                        new Function<String, String>() {
                            @NullableDecl
                            @Override
                            public String apply(@NullableDecl String accessToken) {
                                authBuilder.append( accessToken );
                                return authBuilder.toString();
                            }
                        },
                        MoreExecutors.directExecutor()
                );
            }
            else { // AUTHORIZED: by cached token
                authBuilder.append( getLoggedInUser().getValue().getAccessToken() );
                return Futures.immediateFuture( authBuilder.toString() );
            }
        }
        else { // UNAUTHORIZED: missing credentials
            return Futures.immediateFailedFuture( new AuthException( AuthResultState.UNAUTHORIZED ) );
        }
    }

    public MutableLiveData<LoginCredentials> getLoggedInUser() {
        return this.mLoggedInUser;
    }

    public void setLoggedInUser(LoginCredentials user) {
        if( user == null ) {
            logout();
        }
        else {
            Log.d( TAG, "setting new User " + user );
            this.mLoggedInUser.postValue( user );
            // If user credentials will be cached in local storage, it is recommended it be encrypted
            // @see https://developer.android.com/training/articles/keystore
        }
    }

    public void logout() {
        mLoggedInUser.postValue( null );
    }

    public ListenableFuture<String> signIn(LoginCredentials credentials) {
        Log.d( TAG, "Signing in using " + credentials);
        ListenableFuture<AuthLoginResponse> future = dataSource.login(credentials);
        Futures.addCallback(
                future,
                new FutureCallback<AuthLoginResponse>() {
                    @Override
                    public void onSuccess(@NullableDecl AuthLoginResponse result) {
                        // set expire date pf the access token after 7 days from now
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, 7 );
                        LoginCredentials c = new LoginCredentials( credentials, result.getAccessToken(), cal.getTime() );
                        setLoggedInUser( c );
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e( TAG, "SignIn try Failed", t );
                    }
                },
                getExecutor()
        );

        return Futures.transform(
                future,
                new Function<AuthLoginResponse, String>() {
                    @NullableDecl
                    @Override
                    public String apply(@NullableDecl AuthLoginResponse input) {
                        return input.getAccessToken();
                    }
                },
                getExecutor()
        );
    }

    public ListenableFuture<RegisterCredentials> signUp(RegisterCredentials credentials ) {
        Log.d( TAG, "Signing up using " + credentials);
        ListenableFuture<RegisterCredentials> future = dataSource.register(credentials);
        Futures.addCallback(
                future,
                new FutureCallback<RegisterCredentials>() {
                    @Override
                    public void onSuccess(@NullableDecl RegisterCredentials result) {

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e( TAG, "SignUp try Failed", t );
                    }
                },
                getExecutor()
        );
        return future;
    }
}