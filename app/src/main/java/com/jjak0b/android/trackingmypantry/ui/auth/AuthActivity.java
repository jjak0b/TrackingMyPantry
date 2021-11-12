package com.jjak0b.android.trackingmypantry.ui.auth;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;

// Replace the deprecated AccountAuthenticatorActivity
public class AuthActivity extends AppCompatActivity {
    public static String AUTH_ADD_NEW_ACCOUNT = "new_account";

    private static final String TAG = "AuthActivity";
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    private AuthViewModel viewModel;

    private AppBarConfiguration mAppBarConfiguration;

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

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        mAccountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
            viewModel.setLoggedAccount(null);
        }

        viewModel.onLoggedUser().observe(this, new Observer<LoggedAccount>() {
            @Override
            public void onChanged(LoggedAccount account) {
                if (account == null ) {
                    // let user to pick an account or create a new one
                    Log.d(TAG, "pick");

                }
                else {
                    if( mAccountAuthenticatorResponse != null ) {
                        Log.d(TAG, "set account");

                        Bundle result = new Bundle();
                        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.getAccount().name);
                        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.getAccount().type);
                        // account has been set
                        setAccountAuthenticatorResult(result);
                    }
                    viewModel.onLoggedUser().removeObserver(this::onChanged);
                    finish();
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
        viewModel.onLoggedUser().removeObservers(this);

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