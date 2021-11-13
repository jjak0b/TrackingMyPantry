package com.jjak0b.android.trackingmypantry.ui.auth;

import android.accounts.AccountManager;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.data.auth.LoginResult;
import com.jjak0b.android.trackingmypantry.data.db.entities.User;
import com.jjak0b.android.trackingmypantry.data.repositories.AuthRepository;
import com.jjak0b.android.trackingmypantry.data.services.API.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.services.API.RegisterCredentials;
import com.jjak0b.android.trackingmypantry.services.Authenticator;

import java.io.IOException;

public class AuthViewModel extends AndroidViewModel {

    private static final String TAG = "AuthViewModel";
    private AuthRepository authRepository;
    private LiveEvent<LoginResult> mUIResult;

    private LiveEvent<Resource<Bundle>> onAuthenticate;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = AuthRepository.getInstance(application);
        this.mUIResult = new LiveEvent<>();
        this.onAuthenticate = new LiveEvent<>();
        this.onAuthenticate.setValue(Resource.loading(null));
    }

    public LiveData<Resource<User>> login(
            String email,
            String password
    ) {

        LoginCredentials credentials = new LoginCredentials(email, password);
        return Transformations.map(authRepository.getUser(credentials), resource -> {
            Resource<Bundle> result;
            switch (resource.getStatus()) {
                case SUCCESS:
                    User user = resource.getData();
                    Bundle bundle = new Bundle();
                    bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE);
                    bundle.putString(AccountManager.KEY_ACCOUNT_NAME, credentials.getEmail());
                    bundle.putString(AccountManager.KEY_PASSWORD, credentials.getPassword());
                    result = Resource.success(bundle);
                    break;
                case ERROR:
                    result = Resource.error(resource.getError(), null);
                    break;
                default:
                    result = Resource.loading(null);
                    break;
            }
            onAuthenticate.setValue(result);
            return resource;
        });
    }

    public LiveData<Resource<User>> register(
            String username,
            String email,
            String password
    ) {

        RegisterCredentials credentials = new RegisterCredentials(username,  email, password);

        return Transformations.map(authRepository.addUser(credentials), resource -> {
            if (resource.getStatus() == Status.SUCCESS) {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE);
                result.putString(AccountManager.KEY_ACCOUNT_NAME, credentials.getEmail());
                result.putString(AccountManager.KEY_PASSWORD, credentials.getPassword());
            }
            return resource;
        });
    }

    public void logout() {
        authRepository.setLoggedAccount(null);
    }

    public void setLoggedAccount(String name) {
        authRepository.setLoggedAccount(name);
    }

    public LiveData<Resource<LoggedAccount>> getLoggedAccount() {
        return authRepository.getLoggedAccount();
    }

    public LiveData<Resource<Bundle>> onAuthenticate() {
        return onAuthenticate;
    }
}
