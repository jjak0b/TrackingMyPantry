package com.jjak0b.android.trackingmypantry.ui.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;

// Replace the deprecated AccountAuthenticatorActivity
public class AuthActivity extends AppCompatActivity {
    public static String AUTH_ADD_NEW_ACCOUNT = "new_account";

    private static final String TAG = "AuthActivity";
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    private AuthViewModel authViewModel;

    private AppBarConfiguration mAppBarConfiguration;

    LiveData<Resource<Bundle>> onAuth;
    /**
     * Retrieves the AccountAuthenticatorResponse from either the intent of the icicle, if the
     * icicle is non-zero.
     * @param savedInstanceState the save instance data of this Activity, may be null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        // set UI
        setContentView(R.layout.activity_auth);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_auth
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.auth_nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(toolbar, navController);


        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        mAccountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
            // authViewModel.logout();
        }

        AccountManager mAccountManager = AccountManager.get(getBaseContext());
        onAuth = authViewModel.onAuthenticate();
        onAuth.observe(this, new Observer<Resource<Bundle>>() {
            @Override
            public void onChanged(Resource<Bundle> resource) {
                switch (resource.getStatus()) {
                    case LOADING:
                        break;
                    case ERROR:
                        break;
                    case SUCCESS:
                        if( mAccountAuthenticatorResponse != null ) {
                            Bundle data = resource.getData();

                            Account account = new Account(
                                    data.getString(AccountManager.KEY_ACCOUNT_NAME),
                                    data.getString(AccountManager.KEY_ACCOUNT_TYPE)
                            );

                            if( mAccountManager.addAccountExplicitly(
                                    account,
                                    data.getString(AccountManager.KEY_PASSWORD),
                                    null
                            )) {
                                Log.d(TAG, "New account added: " + account.name );
                            }
                            else {
                                Log.d(TAG, "Account already exists: " + account.name );
                                mAccountManager.setPassword(
                                        account,
                                        data.getString(AccountManager.KEY_PASSWORD)
                                );
                            }

                            Bundle result = new Bundle();
                            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                            // account has been set
                            setAccountAuthenticatorResult(result);
                        }
                        onAuth.removeObserver(this);
                        finish();
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    @Override
    public void finish() {
        if( onAuth != null ){
            onAuth.removeObservers(this);
        }

        if (mAccountAuthenticatorResponse != null) {
            Intent i = new Intent();
            if (mResultBundle != null) {
                i.putExtras(mResultBundle);
                setResult(RESULT_OK, i);
            } else {
                setResult(RESULT_CANCELED, i);
            }
            mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }
}