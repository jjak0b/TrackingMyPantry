package com.jjak0b.android.trackingmypantry.ui.auth;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.app.Application;
import android.util.Log;
import android.util.Patterns;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.LoginRepository;
import com.jjak0b.android.trackingmypantry.data.auth.LoginResult;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.data.model.LoginCredentials;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.RegisterCredentials;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.io.IOException;
import retrofit2.HttpException;

public class AuthViewModel extends AndroidViewModel {

    private static final String TAG = "Auth";

    private LoginRepository loginRepository;
    protected MutableLiveData<LoginFormState> loginFormState;
    protected LiveEvent<LoginResult> loginUIResult;
    private LiveEvent<LoggedAccount> onLoggedAccount;
    @Override
    protected void onCleared() {
        loginFormState = null;
        loginUIResult = null;
        onLoggedAccount.removeSource(loginRepository.getLoggedInUser());
        onLoggedAccount = null;
        loginRepository = null;
        super.onCleared();
    }

    public AuthViewModel(Application application) {
        super(application);
        Log.d( TAG, "new login vm instance");
        this.loginRepository = LoginRepository.getInstance(getApplication());
        this.loginFormState = new MutableLiveData<>( new LoginFormState(false) );
        this.loginUIResult = new LiveEvent<>();
        this.onLoggedAccount = new LiveEvent<>();
        this.onLoggedAccount.addSource(loginRepository.getLoggedInUser(), account -> this.onLoggedAccount.postValue(account) );
    }

    LiveData<LoginFormState> getLoginFormState() { return loginFormState; }

    public LiveData<LoginResult> getLoginUIResult() { return loginUIResult; }

    LiveData<LoggedAccount> getLoggedUser() { return onLoggedAccount; }

    public boolean setLoggedAccount( String name ) {
        return loginRepository.setLoggedAccount( name );
    }

    public boolean isAuthDataValid() {
        return getLoginFormState().getValue().isDataValid();
    }

    public ListenableFuture login(
            String email,
            String password
    ) {

        LoginCredentials user = new LoginCredentials(email, password);

        ListenableFuture future = loginRepository.signIn(user);
        Futures.addCallback(
                future,
                new FutureCallback<String>() {
                    @Override
                    public void onSuccess(@NullableDecl String result) {
                        loginUIResult.postValue(new LoginResult( new LoggedInUserView( user.getEmail() ) ) );
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        setUIErrorFor( t, true);
                    }
                },
                MoreExecutors.newSequentialExecutor( MoreExecutors.directExecutor() )
        );
        return future;
    }

    public ListenableFuture<String> authenticate() {
        return loginRepository.requireAuthorization(false);
    }

    public ListenableFuture register( String username, String email, String password ) {

        RegisterCredentials newUser = new RegisterCredentials( username, email, password );

        ListenableFuture future = loginRepository.signUp(newUser);
        Futures.addCallback(
                future,
                new FutureCallback<RegisterCredentials>() {
                    @Override
                    public void onSuccess(@NullableDecl RegisterCredentials result) {
                        loginUIResult.postValue(new LoginResult( new LoggedInUserView( result.getUsername() ) ) );
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        setUIErrorFor( t, false );
                    }
                },
                MoreExecutors.newSequentialExecutor( MoreExecutors.directExecutor() )
        );
        return future;
    }

    public void setUIErrorFor( Throwable t, boolean isSignIn ) {
        if( t instanceof HttpException) {
            Log.w( TAG, "Server/Authentication Error", t );
            if( isSignIn ) loginUIResult.postValue(new LoginResult( R.string.signIn_failed ) );
            else loginUIResult.postValue(new LoginResult( R.string.signUp_failed ) );
        }
        else if( t instanceof IOException) {
            Log.w( TAG, "Network Error", t );
            loginUIResult.postValue(new LoginResult( R.string.auth_failed_network) );
        }
        else {
            Log.e( TAG, "Unexpected Error", t );
            loginUIResult.postValue(new LoginResult( R.string.auth_failed_unknown ) );
        }
    }

    public void updateFormState(String username, String email, String password) {

        LoginFormState state = new LoginFormState(true);

        updateFormStateInternal( state, username, email, password );

        loginFormState.setValue( state );
    }

    public void updateFormState(String email, String password) {
        LoginFormState state = new LoginFormState(true);

        updateFormStateInternal( state, email, password );

        loginFormState.setValue( state );
    }

    private void updateFormStateInternal( LoginFormState state, String username, String email, String password ) {
        if (!isUsernameValid(username)) {
            state.setUsernameError(R.string.invalid_username);
        }

        updateFormStateInternal( state, email, password );
    }

    private void updateFormStateInternal( LoginFormState state, String email, String password ) {
        if (!isEmailValid(email)) {
            Log.d(TAG, "set email as invalid");
            state.setEmailError(R.string.invalid_email);
        }

        if (!isPasswordValid(password)) {
            Log.d(TAG, "set pass as invalid");
            state.setPasswordError(R.string.invalid_password);
        }
    }

    // A placeholder username validation check
    private boolean isUsernameValid(String username) {
        if (username == null) {
            return false;
        }

        return !username.trim().isEmpty();
    }

    // A placeholder email validation check
    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }
        if (email.trim().contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
        return false;
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        Log.d(TAG, "is pw valid: '" + password + "'");
        return password != null && password.trim().length() > 0;
    }
}