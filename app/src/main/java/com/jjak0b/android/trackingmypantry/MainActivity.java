package com.jjak0b.android.trackingmypantry;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.jjak0b.android.trackingmypantry.data.api.AuthException;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.preferences.Preferences;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.data.api.NotLoggedInException;
import com.jjak0b.android.trackingmypantry.services.Authenticator;
import com.jjak0b.android.trackingmypantry.ui.auth.AuthViewModel;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.ProductOverviewFragmentArgs;
import com.jjak0b.android.trackingmypantry.ui.util.Permissions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import java.io.IOException;

public class MainActivity extends AppCompatActivity  {

    private final static String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private AuthViewModel authViewModel;

    private ActivityResultLauncher<Intent> chooseAccountLauncher;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private SharedPreferences.OnSharedPreferenceChangeListener onEnableFeatureExpirationReminders;

    private LiveData<Resource<LoggedAccount>> mLoggedAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        initNavController(navigationView);
        View headerView = navigationView.getHeaderView(0);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        mLoggedAccount = authViewModel.getLoggedAccount();
        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog.

        Context activityContext = this;
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {

                    boolean areAllGranted = !isGranted.containsValue(false);
                    SharedPreferences options = getSharedPreferences(Preferences.FEATURE_EXPIRATION_REMINDERS.KEY, MODE_PRIVATE);

                    if( areAllGranted ){
                        options.edit()
                                .putBoolean(Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_ENABLED, Preferences.FEATURE_EXPIRATION_REMINDERS.ENABLED)
                                .apply();
                    }
                    else {
                        options.edit()
                                .putBoolean(Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_ENABLED, Preferences.FEATURE_EXPIRATION_REMINDERS.DISABLED)
                                .apply();
                    }

                    mLoggedAccount.observe(this, new Observer<Resource<LoggedAccount>>() {
                        @Override
                        public void onChanged(Resource<LoggedAccount> resource) {
                            switch (resource.getStatus()) {
                                case SUCCESS:
                                    mLoggedAccount.removeObserver(this);
                                    LoggedAccount account = resource.getData();
                                    if (areAllGranted) {
                                        // Permission is granted.

                                        // switch off and on to trigger sync
                                        ContentResolver.setSyncAutomatically(account.getAccount(), CalendarContract.AUTHORITY, false);
                                        ContentResolver.setSyncAutomatically(account.getAccount(), CalendarContract.AUTHORITY, true);
                                    }
                                    else {
                                        // Explain to the user that the feature is unavailable because the
                                        // features requires a permission that the user has denied.
                                        new AlertDialog.Builder(activityContext)
                                                .setTitle(R.string.rationale_title_feature)
                                                .setMessage(R.string.features_calendar_disabled_cause_permissions)
                                                .setPositiveButton(android.R.string.ok, null)
                                                .show();

                                        // switch off sync
                                        ContentResolver.setSyncAutomatically(account.getAccount(), CalendarContract.AUTHORITY, false);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                });

        // Register the account chooser callback, which handles the user's response to the
        // system account dialog.
        chooseAccountLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        Bundle b = intent.getExtras();
                        String accountName = b.getString(AccountManager.KEY_ACCOUNT_NAME);

                        Log.d(TAG, "Selected account to login with: " + accountName );
                        authViewModel.setLoggedAccount( accountName );
                    }
                    else {
                        Toast.makeText(getBaseContext(), R.string.error_account_required, Toast.LENGTH_SHORT )
                                .show();
                        finish();
                    }
                });

        onEnableFeatureExpirationReminders = (sharedPreferences, key) -> {
            if( Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_ENABLED.equals(key) ){
                boolean isEnabled = sharedPreferences.getBoolean(Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_ENABLED, Preferences.FEATURE_EXPIRATION_REMINDERS.DEFAULT);
                if( isEnabled ){
                    requestFeatureExpirationReminders( requestPermissionLauncher, sharedPreferences);
                }
            }
        };

        // Observe first auth result
        observeOnFailedAuth();
        observeOnSuccessAuth();

        initNavHeaderView(headerView);

        // authenticate();
    }

    private void observeOnFailedAuth() {
        Log.d(TAG, "Observing on failed auth");
        mLoggedAccount.observe(this, new Observer<Resource<LoggedAccount>>() {
            @Override
            public void onChanged(Resource<LoggedAccount> resource) {
                switch (resource.getStatus()) {
                    case ERROR:
                        Log.e(TAG, "Observer failed auth", resource.getError());

                        if( resource.getError() instanceof NotLoggedInException ) {
                            Log.d(TAG, "User not logged in");
                            launchAccountChooser(null);
                        }
                        else if( resource.getError() instanceof AuthException ) {
                            // Unauthorized user
                            Toast.makeText(
                                    getBaseContext(),
                                    R.string.signIn_failed,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        else if( resource.getError() instanceof IOException ) {
                            // Network error, unable to contact server
                            Toast.makeText(
                                    getBaseContext(),
                                    R.string.auth_failed_network,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        else { // if( resource.getError() instanceof RemoteException )
                                // Server error, unable to verify user
                                Toast.makeText(
                                        getBaseContext(),
                                        R.string.auth_failed_unknown,
                                        Toast.LENGTH_LONG
                                ).show();
                        }
                        break;
                }
            }
        });
    }

    private void observeOnSuccessAuth() {
        Log.d(TAG, "Observing on success auth");
        SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        options.registerOnSharedPreferenceChangeListener(onEnableFeatureExpirationReminders);

        mLoggedAccount.observe(this, resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    // we have just authenticated
                    LoggedAccount mLoggedAccount = resource.getData();
                    Log.d(TAG, "Observer success auth for account " + mLoggedAccount);

                    // setup reminders for account
                    boolean featureFlag = options.getBoolean(Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_ENABLED, Preferences.FEATURE_EXPIRATION_REMINDERS.DEFAULT);

                    if( featureFlag == Preferences.FEATURE_EXPIRATION_REMINDERS.DEFAULT || featureFlag == Preferences.FEATURE_EXPIRATION_REMINDERS.ENABLED ) {
                        requestFeatureExpirationReminders( requestPermissionLauncher, options);
                        ContentResolver.requestSync(mLoggedAccount.getAccount(), CalendarContract.AUTHORITY, new Bundle() );
                    }
                    break;
            }
        });
    }

    private void initNavController(NavigationView navigationView) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_auth, R.id.nav_products
        )
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            switch (destination.getId()){
                case R.id.productOverviewFragment:
                    getSupportActionBar().setSubtitle(ProductOverviewFragmentArgs.fromBundle(arguments).getSubtitle());
                    break;
                default:
                    getSupportActionBar().setSubtitle(null);
                    break;
            }
        });
    }

    private void initNavHeaderView(@NonNull View navHeaderView ){

        TextView usernameView = navHeaderView.findViewById(R.id.login_info_username);
        TextView emailView = navHeaderView.findViewById(R.id.login_info_email);
        navHeaderView.setClickable(true);

        mLoggedAccount.observe(this, resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    LoggedAccount loggedAccount = resource.getData();
                    usernameView.setText(loggedAccount.getUsername());
                    emailView.setText(loggedAccount.getEmail());
                    navHeaderView.setOnClickListener( v -> launchAccountChooser(loggedAccount.getAccount()));
                    break;
                case ERROR:
                default:
                    usernameView.setText(null);
                    emailView.setText(null);
                    navHeaderView.setOnClickListener( v -> launchAccountChooser(null));
                    break;
            }
        });
    }

    private void requestFeatureExpirationReminders( ActivityResultLauncher<String[]> requestPermissionLauncher, SharedPreferences options) {
        new Permissions.FeatureRequestBuilder()
                .setRationaleMessage(R.string.rationale_msg_features_calendar)
                .setOnPositive(requestPermissionLauncher, new String[] {
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.READ_CALENDAR
                })
                .setOnNegative(R.string.features_calendar_disabled, () -> {
                    options.edit()
                            .putBoolean(Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_ENABLED, Preferences.FEATURE_EXPIRATION_REMINDERS.DISABLED)
                            .apply();
                })
                .show(this);
    }

    private void launchAccountChooser(Account selected) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if( onEnableFeatureExpirationReminders != null )
            options.unregisterOnSharedPreferenceChangeListener(onEnableFeatureExpirationReminders);

        this.mAppBarConfiguration = null;
        this.onEnableFeatureExpirationReminders = null;
        this.authViewModel = null;
        this.chooseAccountLauncher = null;
        this.requestPermissionLauncher = null;
        this.mLoggedAccount.removeObservers(this);
        this.mLoggedAccount = null;

        super.onDestroy();
    }
}