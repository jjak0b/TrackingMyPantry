package com.jjak0b.android.trackingmypantry.ui.auth;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.app.Application;
import android.util.Log;
import android.util.Patterns;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.api.RemoteException;
import com.jjak0b.android.trackingmypantry.data.auth.LoginResult;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.R;
import java.io.IOException;

public class AuthFormViewModel extends AndroidViewModel {

    private static final String TAG = "Auth";
    protected MutableLiveData<LoginFormState> loginFormState;
    protected LiveEvent<LoginResult> mUIResult;
    protected LiveEvent<LoggedAccount> onLoggedAccount;

    @Override
    protected void onCleared() {
        loginFormState = null;
        mUIResult = null;
        onLoggedAccount = null;
        super.onCleared();
    }

    public AuthFormViewModel(Application application) {
        super(application);
        Log.d( TAG, "new login vm instance");
        this.loginFormState = new MutableLiveData<>( new LoginFormState(false) );
        this.mUIResult = new LiveEvent<>();
        this.onLoggedAccount = new LiveEvent<>();
    }

    LiveData<LoginFormState> getLoginFormState() { return loginFormState; }

    public LiveData<LoginResult> getLoginUIResult() { return mUIResult; }

    public boolean isAuthDataValid() {
        return getLoginFormState().getValue().isDataValid();
    }

    public void setUIUser( LoggedInUserView userView ){
        mUIResult.postValue(new LoginResult( userView));
    }

    public void setUIError(Throwable t, boolean isSignIn ) {
        if( t instanceof RemoteException) {
            Log.w( TAG, "Server/Authentication Error", t );
            if( isSignIn ) mUIResult.postValue(new LoginResult( R.string.signIn_failed ) );
            else mUIResult.postValue(new LoginResult( R.string.signUp_failed ) );
        }
        else if( t instanceof IOException) {
            Log.w( TAG, "Network Error", t );
            mUIResult.postValue(new LoginResult( R.string.auth_failed_network) );
        }
        else {
            Log.e( TAG, "Unexpected Error", t );
            mUIResult.postValue(new LoginResult( R.string.auth_failed_unknown ) );
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