package com.jjak0b.android.trackingmypantry;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.data.api.AuthException;
import com.jjak0b.android.trackingmypantry.data.api.NotLoggedInException;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.services.Authenticator;
import com.jjak0b.android.trackingmypantry.ui.auth.AuthViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;

import java.io.IOException;

/**
 * Activity class that launch the {@link AccountManager#newChooseAccountIntent} wizard if user is not logged in
 */
public abstract class UserAuthActivity extends AppCompatActivity {

    private final static String TAG = "UserAuthActivity";

    private AuthViewModel authViewModel;

    private ActivityResultLauncher<Intent> chooseAccountLauncher;

    private LiveData<Resource<LoggedAccount>> mLoggedAccount;

    public AuthViewModel getAuthViewModel() {
        return authViewModel;
    }

    public AuthViewModel initAuthViewModel() {
        return new ViewModelProvider(this).get(AuthViewModel.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authViewModel = initAuthViewModel();
        mLoggedAccount = authViewModel.getLoggedAccount();

        // Register the account chooser callback, which handles the user's response to the
        // system account dialog.
        chooseAccountLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        Bundle b = intent.getExtras();
                        String accountName = b.getString(AccountManager.KEY_ACCOUNT_NAME);

                        Log.d(TAG, "Selected account to login with: " + accountName );
                        getAuthViewModel().setLoggedAccount( accountName );
                    }
                    else {
                        getAuthViewModel().setLoggedAccount( null );
                    }
                });

        // Observe first auth result
        initOnFailedAuth();
        initOnSuccessAuth();
    }

    public void launchAccountChooser(@Nullable Account selected) {
        Intent intent = AccountManager
                .newChooseAccountIntent(
                        selected,
                        null,
                        new String[]{Authenticator.ACCOUNT_TYPE},
                        getString(R.string.description_account_required),
                        Authenticator.TOKEN_TYPE,
                        null,
                        null
                );

        chooseAccountLauncher.launch(intent);
    }

    public void initOnFailedAuth() {
        mLoggedAccount.observe(this, new Observer<Resource<LoggedAccount>>() {
            @Override
            public void onChanged(Resource<LoggedAccount> resource) {
                Account account = resource.getData() != null ? resource.getData().getAccount() : null;
                switch (resource.getStatus()) {
                    case ERROR:
                        String error = ErrorsUtils.getErrorMessage(getBaseContext(), resource.getError(), TAG);
                        if( resource.getError() instanceof NotLoggedInException) {
                            error = null;
                            launchAccountChooser(null);
                        }
                        else if( resource.getError() instanceof AuthException) {
                            // Unauthorized user
                            error = getString(R.string.signIn_failed);
                        }
                        else if( resource.getError() instanceof IOException) {
                            // Network error, unable to contact server
                            error = getString(R.string.auth_failed_network);
                        }
                        else { // if( resource.getError() instanceof RemoteException )
                            // Server error, unable to verify user
                        }

                        if( error != null ) {
                            new AlertDialog.Builder(UserAuthActivity.this)
                                    .setTitle(android.R.string.dialog_alert_title)
                                    .setMessage(error)
                                    .setPositiveButton(R.string.action_retry, (dialogInterface, i) -> {
                                        launchAccountChooser(account);
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                        break;
                }
            }
        });
    }

    public abstract void initOnSuccessAuth();

    @Override
    protected void onDestroy() {

        this.authViewModel = null;
        this.chooseAccountLauncher = null;

        this.mLoggedAccount.removeObservers(this);
        this.mLoggedAccount = null;

        super.onDestroy();
    }
}
