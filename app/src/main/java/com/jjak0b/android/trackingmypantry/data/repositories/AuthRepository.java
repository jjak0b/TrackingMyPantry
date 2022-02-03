package com.jjak0b.android.trackingmypantry.data.repositories;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.AuthException;
import com.jjak0b.android.trackingmypantry.data.api.IOBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.NetworkBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.NotLoggedInException;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.auth.AuthResultState;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.data.dataSource.LoginDataSource;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.UserDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.User;
import com.jjak0b.android.trackingmypantry.data.services.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.services.API.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.services.API.RegisterCredentials;
import com.jjak0b.android.trackingmypantry.services.Authenticator;

import java.io.IOException;

public class AuthRepository {
    private static final String TAG = "AuthRepository";

    private static volatile AuthRepository instance;
    private static final Object sInstanceLock = new Object();

    private LoginDataSource dataSource;
    private AccountManager mAccountManager;
    private PantryDB pantryDB;
    private UserDao userDao;
    private AppExecutors appExecutors;

    private MediatorLiveData<Resource<LoggedAccount>> mLoggedAccount;
    private LiveData<Resource<LoggedAccount>> mLoggedAccountResource;

    // private constructor : singleton access
    private AuthRepository(LoginDataSource dataSource, final Context context ) {
        this.dataSource = dataSource;
        this.mAccountManager = AccountManager.get(context);
        this.appExecutors = AppExecutors.getInstance();
        this.pantryDB = PantryDB.getInstance(context);
        this.userDao = pantryDB.getUserDao();

        this.mLoggedAccount = new MediatorLiveData<>();
        this.mLoggedAccount.setValue(Resource.error(new NotLoggedInException(), null));
    }

    public static AuthRepository getInstance(final Context context) {
        return getInstance( LoginDataSource.getInstance(), context);
    }

    public static AuthRepository getInstance(LoginDataSource dataSource, final Context context) {
        AuthRepository i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new AuthRepository(dataSource, context);
                    i = instance;
                }
            }
        }
        return i;
    }

    public void setLoggedAccount(String name) {

        Account account = name != null ? getAccount(name) : null;
        LiveData<Resource<LoggedAccount>> mSource;
        // attach a new account resource
        if( account != null ) {
            mSource = getLoggedAccount(account);
        }
        else {
            mSource = new MutableLiveData<>(Resource.error(
                    new NotLoggedInException(),
                    null
            ));
        }

        mLoggedAccount.addSource(mSource, resource -> {
            mLoggedAccount.setValue(resource);
        });

        // detach current account resource
        if( mLoggedAccountResource != null ) {
            mLoggedAccount.removeSource(mLoggedAccountResource);
        }
        mLoggedAccountResource = mSource;
    }

    public LiveData<Resource<LoggedAccount>> getLoggedAccount() {
        return mLoggedAccount;
    }

    public LiveData<Resource<User>> addUser(RegisterCredentials credentials) {
        return signUp(credentials);
    }

    public LiveData<Resource<User>> getUser(LoginCredentials credentials) {
        final MutableLiveData<User> mUser = new MutableLiveData<>(null);
        return new NetworkBoundResource<User, User>(appExecutors) {

            @Override
            protected void saveCallResult(User user) {
                mUser.postValue(user);
            }

            @Override
            protected boolean shouldFetch(@Nullable User data) {
                return true;
            }

            @Override
            protected LiveData<User> loadFromDb() {
                return mUser;
            }

            @Override
            protected LiveData<ApiResponse<User>> createCall() {
                return Transformations.switchMap(signIn(credentials), resource -> {
                    return dataSource.whoAmI(resource.getData());
                });
            }
        }.asLiveData();
    }

    private LiveData<Resource<User>> signUp(RegisterCredentials credentials ) {
        final MutableLiveData<User> mOnSignUp = new MutableLiveData<>(null);
        return new NetworkBoundResource<User, User>(appExecutors) {

            @Override
            protected void saveCallResult(User item) {
                mOnSignUp.postValue(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable User data) {
                return true;
            }

            @Override
            protected void onFetchFailed(Throwable cause) {
                Log.e( TAG, "SignUn try Failed", cause);
            }

            @Override
            protected LiveData<User> loadFromDb() {
                return mOnSignUp;
            }

            @Override
            protected LiveData<ApiResponse<User>> createCall() {
                return dataSource.register(credentials);
            }
        }.asLiveData();
    }

    public LiveData<Resource<String>> signIn(LoginCredentials credentials) {
        final MutableLiveData<String> mAuthToken = new MutableLiveData<>(null);
        return new NetworkBoundResource<String, AuthLoginResponse>(appExecutors) {
            @Override
            protected void saveCallResult(AuthLoginResponse item) {
                mAuthToken.postValue(item.getAccessToken());
            }

            @Override
            protected boolean shouldFetch(@Nullable String data) {
                return true;
            }

            @Override
            protected void onFetchFailed(Throwable cause) {
                Log.e( TAG, "SignIn try Failed", cause );
            }

            @Override
            protected LiveData<String> loadFromDb() {
                return mAuthToken;
            }

            @Override
            protected LiveData<ApiResponse<AuthLoginResponse>> createCall() {
                return dataSource.login(credentials);
            }
        }.asLiveData();
    }

    /**
     *
     * @param account
     * @return
     */
    private LiveData<Resource<LoggedAccount>> getLoggedAccount(@NonNull Account account ) {
        // User have to logged almost once to get a valid account, otherwise will fail even if offline
        String userID = mAccountManager.getUserData(account, Authenticator.ACCOUNT_ID );
        final LiveData<User> mUser = userDao.get(userID);

        final MutableLiveData<Resource<LoggedAccount>> mLoggedAccount = new MutableLiveData<>(Resource.loading(null));
        final LiveData<Resource<User>> fetchedUser = new NetworkBoundResource<User, User>(appExecutors) {
            @Override
            protected void saveCallResult(User fetchedUser) {
                userDao.updateOrInsert(fetchedUser);
            }

            @Override
            protected boolean shouldFetch(@Nullable User data) {
                return data == null;
            }

            @Override
            protected void onFetchFailed(Throwable cause) {
                Log.e( TAG, "Unable to fetch account info", cause);
            }

            @Override
            protected LiveData<User> loadFromDb() {
                return mUser;
            }

            @Override
            protected LiveData<ApiResponse<User>> createCall() {
                return Transformations.switchMap(getAuthToken(account), resource -> {
                    return dataSource.whoAmI(resource.getData());
                });
            }
        }.asLiveData();

        return Transformations.forwardOnce(fetchedUser, resourceFetchedUser -> {
            String user_id = resourceFetchedUser.getData().getId();
            return Transformations.forward(
                    IOBoundResource.adapt(appExecutors, userDao.get(user_id)),
                    resourceUser -> {
                        User user = resourceUser.getData();
                        if( user != null ) {
                            mLoggedAccount.setValue(Resource.success(new LoggedAccount.Builder()
                                    .setAccount(account)
                                    .setUser(user)
                                    .build()
                            ));
                        }
                        else {
                            mLoggedAccount.setValue(Resource.loading(null));
                        }

                        return mLoggedAccount;
                    }
            );
        });
    }

    /**
     * Like {@link #getAuthTokenFromAccountManager(Account)} but it will silent test the auth validity first
     * @param account
     * @return a Resource containing the value of the auth token or an occurred error:
     * if an error occurs the resource contains:
     * <ul>
     * <li>{@link AuthException} if the authenticator failed to respond: failed authentication</li>
     * <li>{@link IOException } if the authenticator experienced an I/O problem creating a new auth token, usually because of network trouble</li>
     * <li>{@link OperationCanceledException} if the operation is canceled for any reason, includidng the user canceling a credential request</li>
     * </ul>
     */
    private LiveData<Resource<String>> getAuthToken(@NonNull Account account) {
        // request a token, first attempt ( can be from cache or authenticator )
        return Transformations.forward(getAuthTokenFromAccountManager(account), rTestToken -> {
            final MediatorLiveData<Resource<String>> mAuthToken = new MediatorLiveData<>();

            // do a request to test it's validity
            LiveData<Resource<String>> mTestSource = testTokenValidity(rTestToken.getData());

            mAuthToken.addSource(mTestSource, rValidToken -> {
                switch (rValidToken.getStatus()) {
                    case ERROR:
                        // Unauthorized ...
                        if( rValidToken.getError() instanceof AuthException ) {
                            // Invalidate token
                            Log.e(TAG, "Got Invalid token while testing auth token", rValidToken.getError() );
                            mAccountManager.invalidateAuthToken(Authenticator.TOKEN_TYPE, rTestToken.getData() );
                            // Detach Invalid token's source
                            mAuthToken.removeSource(mTestSource);
                            Log.d(TAG, "Renew auth token");
                            // Attach new renewed token's source
                            mAuthToken.addSource(getAuthTokenFromAccountManager(account), mAuthToken::setValue );
                        }
                        mAuthToken.setValue(rValidToken);
                        break;
                    default:
                        mAuthToken.setValue(rValidToken);
                        break;
                }
            });

            return mAuthToken;
        });
    }

    public LiveData<Resource<String>> testTokenValidity(String authToken) {
        final MutableLiveData<String> mToken = new MutableLiveData<>(authToken);
        return new NetworkBoundResource<String, User>(appExecutors) {

            @Override
            protected void saveCallResult(User item) {
                mToken.postValue(authToken);
            }

            @Override
            protected boolean shouldFetch(@Nullable String data) {
                return true;
            }

            @Override
            protected LiveData<String> loadFromDb() {
                return mToken;
            }

            @Override
            protected LiveData<ApiResponse<User>> createCall() {
                return dataSource.whoAmI(authToken);
            }
        }.asLiveData();
    }

    /**
     * Provide a live data of an auth token for the provided account,
     * @param account
     * @return a Resource containing the value of the auth token or an occurred error:
     * if an error occurs the resource contains:
     * <ul>
     * <li>{@link AuthException} if the authenticator failed to respond: failed authentication</li>
     * <li>{@link IOException } if the authenticator experienced an I/O problem creating a new auth token, usually because of network trouble</li>
     * <li>{@link OperationCanceledException} if the operation is canceled for any reason, includidng the user canceling a credential request</li>
     * </ul>
     */
    private LiveData<Resource<String>> getAuthTokenFromAccountManager(@NonNull Account account) {

        return Transformations.simulateApi(appExecutors.networkIO(), appExecutors.mainThread(), () -> {
            try {
                return mAccountManager.blockingGetAuthToken(account, Authenticator.TOKEN_TYPE, true );
            }
            catch (IOException | OperationCanceledException e) {
                e.printStackTrace();
                throw e;
            }
            catch (AuthenticatorException e) {
                e.printStackTrace();
                throw new AuthException(AuthResultState.UNAUTHORIZED);
            }
        });
    }

    /**
     * Provide a rsource of an auth token for the current logged account
     * @return a Live data of a resource containing the auth token if the operation has be completed with success
     * Otherwise return a Resource containing an error.
     * If an error occurs the resource contains:
     * <ul>
     * <li>Errors of {@link #getLoggedAccount(Account)}</li>
     * <li>Errors of {@link #getAuthToken(Account)}</li>
     * </ul>
     */
    public LiveData<Resource<String>> requireAuthorization() {
        final MutableLiveData<String> mAuthToken = new MutableLiveData<>(null);

        return new NetworkBoundResource<String, String>(appExecutors) {

            @Override
            protected void saveCallResult(String item) {
                mAuthToken.postValue(new StringBuilder()
                        .append(Authenticator.TOKEN_TYPE)
                        .append(" ")
                        .append(item)
                        .toString());
            }

            @Override
            protected boolean shouldFetch(@Nullable String data) {
                return data == null;
            }

            @Override
            protected LiveData<String> loadFromDb() {
                return mAuthToken;
            }

            @Override
            protected LiveData<ApiResponse<String>> createCall() {
                // require a logged account
                return Transformations.switchMap(getLoggedAccount(), resourceAccount -> {
                    // require an authentication token for this account
                    return Transformations.adapt(getAuthToken(resourceAccount.getData().getAccount()));
                });
            }
        }.asLiveData();
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
    
}
