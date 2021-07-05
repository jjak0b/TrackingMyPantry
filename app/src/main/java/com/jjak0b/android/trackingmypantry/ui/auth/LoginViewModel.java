package com.jjak0b.android.trackingmypantry.ui.auth;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.app.Application;
import android.util.Log;
import android.util.Patterns;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.LoginRepository;
import com.jjak0b.android.trackingmypantry.data.Result;
import com.jjak0b.android.trackingmypantry.data.auth.AuthResultState;
import com.jjak0b.android.trackingmypantry.data.dataSource.LoginDataSource;
import com.jjak0b.android.trackingmypantry.data.model.LoginCredentials;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.RegisterCredentials;

import java9.util.concurrent.CompletableFuture;

public class LoginViewModel extends AndroidViewModel {

    private static final String TAG = "Auth";

    private LoginRepository loginRepository;
    protected MutableLiveData<LoginFormState> loginFormState;
    protected LiveEvent<LoginResult> loginUIResult;


    public LoginViewModel( Application application) {
        super(application);
        Log.d( TAG, "new login vm instance");
        this.loginRepository = LoginRepository.getInstance(LoginDataSource.getInstance());
        this.loginFormState = new MutableLiveData<>( new LoginFormState(false) );
        this.loginUIResult = new LiveEvent<>();
    }

    LiveData<LoginFormState> getLoginFormState() { return loginFormState; }

    LiveData<LoginResult> getLoginUIResult() { return loginUIResult; }

    LiveData<LoginCredentials> getLoggedUser() { return loginRepository.getLoggedInUser(); }

    public boolean isAuthDataValid() {
        return getLoginFormState().getValue().isDataValid();
    }

    public CompletableFuture<Void> login(
            String email,
            String password
    ) {

        LoginCredentials user = new LoginCredentials(email, password);

        return loginRepository.signIn(user)
                .thenAccept(result -> {
                    if( result instanceof Result.Success ) {
                        LoginCredentials loggedUser = loginRepository.getLoggedInUser().getValue();
                        loginUIResult.setValue(new LoginResult( new LoggedInUserView( loggedUser.getEmail() ) ) );
                    }
                    else {
                        Result.Error<AuthResultState, AuthResultState> error
                                = (Result.Error<AuthResultState, AuthResultState>) result;
                        switch (error.getError()) {
                            case UNAUTHORIZED:
                                loginUIResult.setValue(new LoginResult( R.string.signIn_failed ) );
                                break;
                            case FAILED:
                                loginUIResult.setValue(new LoginResult( R.string.operation_failed_network ) );
                                break;
                        }
                    }
                });
    }

    public CompletableFuture<Void> register( String username, String email, String password ) {

        RegisterCredentials newUser = new RegisterCredentials( username, email, password );

        return loginRepository.signUp(newUser)
                .thenAccept( result -> {
                    if( result instanceof Result.Success ) {
                        loginUIResult.setValue(new LoginResult( new LoggedInUserView(newUser.getUsername()) ) );
                    }
                    else {
                        Result.Error<AuthResultState, AuthResultState> error
                                = (Result.Error<AuthResultState, AuthResultState>) result;
                        switch (error.getError()) {
                            case UNAUTHORIZED:
                                loginUIResult.setValue(new LoginResult( R.string.signUp_failed ) );
                                break;
                            case FAILED:
                                loginUIResult.setValue(new LoginResult( R.string.operation_failed_network ) );
                                break;
                        }
                    }
                });
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