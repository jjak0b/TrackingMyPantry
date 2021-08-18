package com.jjak0b.android.trackingmypantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.LoginRepository;

public class MainViewModel extends AndroidViewModel {
    LoginRepository authRepo;

    public MainViewModel(@NonNull Application application) {
        super(application);
        authRepo = LoginRepository.getInstance(application);
    }

    @Override
    protected void onCleared() {
        authRepo = null;
        super.onCleared();
    }

    ListenableFuture<String> authenticate() {
        return authRepo.requireAuthorization(false);
    }

    boolean setLoggedAccount( String name ) {
        return authRepo.setLoggedAccount( name );
    }
}
