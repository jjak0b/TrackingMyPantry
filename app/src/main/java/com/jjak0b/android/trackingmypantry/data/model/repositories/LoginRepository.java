package com.jjak0b.android.trackingmypantry.data.model.repositories;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.jjak0b.android.trackingmypantry.data.auth.NotLoggedInException;
import com.jjak0b.android.trackingmypantry.data.dataSource.LoginDataSource;
import com.jjak0b.android.trackingmypantry.data.model.services.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.data.model.services.API.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.model.services.API.RegisterCredentials;
import com.jjak0b.android.trackingmypantry.data.model.entities.User;
import com.jjak0b.android.trackingmypantry.services.Authenticator;


import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.concurrent.Executors;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static final String TAG = "LoginRepository";

    private static volatile LoginRepository instance;
    private static final Object sInstanceLock = new Object();

    private LoginDataSource dataSource;


    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private MutableLiveData<LoggedAccount> mLoggedInUser;
    private LiveEvent<AuthResultState> mAuthResultState;
    private AccountManager mAccountManager;

    private static final int nTHREADS = 2;
    private static final ListeningExecutorService executor =
            MoreExecutors.listeningDecorator( Executors.newFixedThreadPool(nTHREADS) );
    private Context context;
    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource, final Context context ) {
        this.dataSource = dataSource;
        this.mAccountManager = AccountManager.get(context);
        this.context = context;
    }

    public static LoginRepository getInstance(final Context context) {
        return getInstance( LoginDataSource.getInstance(), context);
    }

    public static LoginRepository getInstance(LoginDataSource dataSource, final Context context) {
        LoginRepository i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new LoginRepository(dataSource, context);
                    instance.mLoggedInUser = new MutableLiveData<>(null);
                    i = instance;
                }
            }
        }
        return i;
    }

    public ListeningExecutorService getExecutor(){
        return executor;
    }

    public boolean isLoggedIn() {
        return mLoggedInUser.getValue() != null;
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
        Log.d(TAG, "Request Auth");
        // if( isLoggedIn() ){
            // LoginCredentials credentials = getLoggedInUser().getValue();

            // if( forceRefresh || credentials.isAccessTokenExpired() ){
            ListenableFuture<Account> futureAccount;

                final LoggedAccount loggedAccount = getLoggedAccount();
                if( loggedAccount != null) {
                    Log.d(TAG, "using current account");
                    futureAccount = Futures.immediateFuture(loggedAccount.getAccount());
                }
                else {
                    Log.d(TAG, "request to create an account");
                    futureAccount = Futures.immediateFailedFuture(new NotLoggedInException());
                }

                ListenableFuture<String> futureAuthToken = Futures.transformAsync(
                        futureAccount,
                        this::requestAuthTokenForAccount,
                        getExecutor()
                );

                return Futures.transform(
                        futureAuthToken,
                        new Function<String, String>() {
                            @NullableDecl
                            @Override
                            public String apply(@NullableDecl String accessToken) {
                                Log.d(TAG, "request auth completed");
                                authBuilder.append( accessToken );
                                return authBuilder.toString();
                            }
                        },
                        getExecutor()
                );
        // }
        // else { // UNAUTHORIZED: missing credentials
        //     return Futures.immediateFailedFuture( new AuthException( AuthResultState.UNAUTHORIZED ) );
        // }
    }

    @Nullable
    private LoggedAccount getLoggedAccount() {
        if( isLoggedIn() ) {
            return getLoggedInUser().getValue();
        }
        return null;
    }

    @Nullable
    public Account getAccount(String name) {
        Account[] accounts = mAccountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (account.name.equals(name)) {
                return account;
            }
        }
        return null;
    }

    public MutableLiveData<LoggedAccount> getLoggedInUser() {
        return this.mLoggedInUser;
    }

    public void setLoggedInUser(LoggedAccount user) {
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
        Log.d( TAG, "logging out"  );
        mLoggedInUser.postValue( null );
    }

    public boolean setLoggedAccount( String name ){
        if( name != null ) {
            if( isLoggedIn() ) {
                if( getLoggedInUser().getValue()
                        .getName().equals(name)) {
                    return true;
                }
            }
            Account account = getAccount(name);

            if (account != null) {
                setLoggedInUser(new LoggedAccount.Builder()
                        .setAccount(account)
                        .setUser(this.buildUserFromExistingAccount(account))
                        .build()
                );
                return true;
            }
        }
        else if( isLoggedIn() ){
            setLoggedInUser(null);
        }
        return false;
    }

    /**
     * @Precondition: Create a bundle to store into an {Account}
     * @param account
     * @return
     */
    private static Bundle buildBundleFromUser(@NonNull User user) {
        Bundle bundle = new Bundle();
        bundle.putString("id", user.getId());
        bundle.putString("username", user.getUsername());
        return bundle;
    }

    /**
     * @Precondition: Account must an already registered account into device
     * @param account
     * @return
     */
    private User buildUserFromExistingAccount(Account account) {
        return new User(
                mAccountManager.getUserData(account, "id"),
                mAccountManager.getUserData(account, "username")
        );
    }

    public ListenableFuture<String> signIn(LoginCredentials credentials) {
        Log.d( TAG, "Signing in using " + credentials);
        ListenableFuture<AuthLoginResponse> futureLogin = dataSource.login(credentials);


        ListenableFuture<String> futureAccessToken = Futures.transform(
                futureLogin,
                new Function<AuthLoginResponse, String>() {
                    @NullableDecl
                    @Override
                    public String apply(@NullableDecl AuthLoginResponse input) {
                        return input.getAccessToken();
                    }
                },
                getExecutor()
        );

        ListenableFuture<User> futureUserInfo = Futures.transformAsync(futureAccessToken,
                input -> {
                    StringBuilder authBuilder = new StringBuilder()
                            .append( "Bearer ")
                            .append( input );
                    return getUserInfo(authBuilder.toString());
                },
                getExecutor()
        );

        Futures.addCallback(futureUserInfo,
                new FutureCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        LoggedAccount account = new LoggedAccount.Builder()
                                .setAccount(new Account(credentials.getEmail(), Authenticator.ACCOUNT_TYPE))
                                .setUser(user)
                                .build();
                        Bundle userdata = buildBundleFromUser(user);

                        mAccountManager.addAccountExplicitly( account.getAccount() , credentials.getPassword(), userdata );
                        setLoggedInUser( account );
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e( TAG, "SignIn try Failed", t );
                    }
                },
                getExecutor()
        );

        return futureAccessToken;
    }

    public ListenableFuture<User> getUserInfo(@NonNull String accessToken) {
        return dataSource.whoAmI(accessToken);
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

    boolean createAccountOnDevice(Context context, LoginCredentials loginCredentials ){
        AccountManager accountManager = AccountManager.get(context);

        Account account = new Account(loginCredentials.getEmail(), Authenticator.ACCOUNT_TYPE);

        // If the password doesn't exist, the account doesn't exist
        if( accountManager.getPassword(account) == null ) {
            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            return accountManager.addAccountExplicitly(account, loginCredentials.getPassword(), null);
        }
        return false;
    }

    ListenableFuture<String> requestAuthTokenForAccount( Account account ) {

        ListenableFuture<Bundle> futureAuthTokenBundle = getExecutor().submit(() -> {
            return mAccountManager.getAuthToken (
                    account,
                    Authenticator.TOKEN_TYPE,
                    null,
                    true,
                    null,
                    null
            ).getResult();
        });

        return Futures.transform(
                futureAuthTokenBundle,
                new Function<Bundle, String>() {
                    @NullableDecl
                    @Override
                    public String apply(@NullableDecl Bundle result) {
                        return result.getString( AccountManager.KEY_AUTHTOKEN );
                    }
                },
                getExecutor()
        );
    }
}