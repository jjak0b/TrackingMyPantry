package com.jjak0b.android.trackingmypantry.services;

import android.content.Intent;
import android.os.IBinder;

import androidx.lifecycle.LifecycleService;

public class AuthenticatorService extends LifecycleService {

    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        super.onCreate();
        mAuthenticator = new Authenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return mAuthenticator.getIBinder();
    }
}