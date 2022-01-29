package com.jjak0b.android.trackingmypantry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.jjak0b.android.trackingmypantry.data.api.NotLoggedInException;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;

/**
 * Activity used as splash screen to force user to choose an account to login with
 * before using the main app, otherwise after 2 "cancel" attempts will close the activity
 */
public class SplashActivity extends UserAuthActivity {

    // count of attempts made by user
    // who clicked cancel/back button pressed when account chooser show up
    private int onNotLoggedInAttempts = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Here will init also some app heavy stuff like DB, etc ...
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initOnFailedAuth() {

        LiveData<Resource<LoggedAccount>> mLoggedAccount = getAuthViewModel().getLoggedAccount();
        mLoggedAccount.observe(this, new Observer<Resource<LoggedAccount>>() {
            @Override
            public void onChanged(Resource<LoggedAccount> resource) {
                if( resource.getStatus() != Status.ERROR ) return;

                if( resource.getError() instanceof NotLoggedInException ) {
                    onNotLoggedInAttempts++;
                }
                else {
                    // reset count down
                    onNotLoggedInAttempts = 0;
                }

                if( onNotLoggedInAttempts >= 2) {
                    mLoggedAccount.removeObservers(SplashActivity.this);
                    Toast.makeText(getBaseContext(), R.string.error_account_required, Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
            }
        });

        // init super later so the observer of this function run first
        // so we can terminate activity before trigger the account chooser wizard
        super.initOnFailedAuth();
    }

    @Override
    public void initOnSuccessAuth() {
        LiveData<Resource<LoggedAccount>> mLoggedAccount = getAuthViewModel().getLoggedAccount();
        mLoggedAccount.observe(this, new Observer<Resource<LoggedAccount>>() {
            @Override
            public void onChanged(Resource<LoggedAccount> resource) {
                if( resource.getStatus() != Status.SUCCESS ) return;
                mLoggedAccount.removeObserver(this);

                // Starts main app experience
                startActivity(new Intent(getBaseContext(), MainActivity.class));
                finish();
            }
        });
    }
}