package com.jjak0b.android.trackingmypantry.services;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.AuthException;
import com.jjak0b.android.trackingmypantry.data.api.RemoteException;
import com.jjak0b.android.trackingmypantry.data.repositories.AuthRepository;
import com.jjak0b.android.trackingmypantry.ui.auth.AuthActivity;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.services.API.LoginCredentials;

import java.io.IOException;
import java.util.Date;


/*
 * Implement AbstractAccountAuthenticator and stub out all
 * of its methods
 */
public class Authenticator extends AbstractAccountAuthenticator {

    private static final String TAG = Authenticator.class.getName();
    public static final String ACCOUNT_TYPE = "lam21.modron.network";
    public static final String TOKEN_TYPE = "Bearer";
    public static final long TOKEN_EXPIRY_TIME = (7 * 24 * 60 * 60 * 1000);

    private LifecycleService mContext;
    private AuthRepository authRepo;
    public Authenticator(LifecycleService context) {
        super(context);
        mContext = context;
        authRepo = AuthRepository.getInstance(context);
    }

    @NonNull
    public LifecycleOwner getLifecycleOwner() {
        return mContext;
    }

    // Editing properties is not supported
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d( TAG, "addAccount of type " + accountType );
        final Intent intent = new Intent(mContext, AuthActivity.class)
                .putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();

        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    // Ignore attempts to confirm credentials
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d( TAG, "getAuthToken" );
        final Bundle result = new Bundle();
        Log.e( TAG, "AUTHTYPE: " + authTokenType );

        final AccountManager accountManager = AccountManager.get(mContext);
        String authToken = accountManager.peekAuthToken( account, authTokenType );

        // But if the key is invalid or expired ...
        if( TextUtils.isEmpty(authToken) )
        {
            // ...loads the account user credentials to try to authenticate it.
            long expireTime = new Date().getTime() + TOKEN_EXPIRY_TIME;


            AppExecutors.getInstance().mainThread().execute(() -> {
                authRepo.signIn(new LoginCredentials(account.name, accountManager.getPassword(account)))
                        .observe(getLifecycleOwner(), resource -> {
                            switch (resource.getStatus()) {
                                case ERROR:
                                    Throwable t = resource.getError();
                                    if( t instanceof AuthException ) {
                                        Log.w( TAG, "Authentication Error", t );
                                        AuthException error = (AuthException) t;
                                        response.onError(AccountManager.ERROR_CODE_BAD_AUTHENTICATION, mContext.getResources().getString(R.string.signIn_failed ));
                                    }
                                    else if( t instanceof RemoteException) {
                                        Log.w( TAG, "Server Error", t );
                                        response.onError( AccountManager.ERROR_CODE_REMOTE_EXCEPTION, mContext.getResources().getString(R.string.signIn_failed ));
                                    }
                                    else if( t instanceof IOException) {
                                        IOException e = (IOException) t;
                                        Log.w( TAG, "Network Error", t );
                                        response.onError(AccountManager.ERROR_CODE_NETWORK_ERROR, mContext.getResources().getString(R.string.auth_failed_network));
                                    }
                                    else {
                                        Log.e( TAG, "Unexpected Error", t );
                                        response.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS, mContext.getResources().getString(R.string.auth_failed_unknown));
                                    }
                                    break;
                                case SUCCESS:
                                    String mAuthToken = resource.getData();
                                    Log.d( TAG, "got authToken successfully: " + mAuthToken );
                                    result.putString( AccountManager.KEY_ACCOUNT_NAME, account.name );
                                    result.putString( AccountManager.KEY_ACCOUNT_TYPE, account.type );
                                    result.putString( AccountManager.KEY_AUTHTOKEN, mAuthToken );
                                    result.putLong( KEY_CUSTOM_TOKEN_EXPIRY, expireTime );
                                    response.onResult( result );
                                    break;
                                default:
                                    break;
                            }
                        });
            });

            // Returns null because we use the response parameter. See callbacks above.
            return null;
        }
        // Otherwise, the key is valid, it returns.
        result.putString( AccountManager.KEY_ACCOUNT_NAME, account.name );
        result.putString( AccountManager.KEY_ACCOUNT_TYPE, account.type );
        result.putString( AccountManager.KEY_AUTHTOKEN, authToken );
        return result;
    }

    // Getting a label for the auth token is not supported
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        throw new UnsupportedOperationException();
    }

    // Updating user credentials is not supported
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    // Checking features for the account is not supported
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}
