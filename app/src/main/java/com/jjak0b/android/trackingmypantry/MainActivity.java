package com.jjak0b.android.trackingmypantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.data.preferences.Preferences;
import com.jjak0b.android.trackingmypantry.ui.auth.AuthViewModel;

public class MainActivity extends UserAuthActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;

    private ActivityResultLauncher<String[]> requestExpirationReminderFeature;

    private LiveData<Resource<LoggedAccount>> mLoggedAccount;
    private FeaturesViewModel featuresViewModel;

    @Override
    public AuthViewModel initAuthViewModel() {
        AuthViewModel viewModel = super.initAuthViewModel();
        mLoggedAccount = viewModel.getLoggedAccount();
        return viewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        options.registerOnSharedPreferenceChangeListener(this);

        featuresViewModel = new ViewModelProvider(this).get(FeaturesViewModel.class);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        initNavController(navigationView);
        View headerView = navigationView.getHeaderView(0);

        initNavHeaderView(headerView);

    }

    @Override
    public void initOnFailedAuth() {
        super.initOnFailedAuth();
    }

    @Override
    public void initOnSuccessAuth() {
        SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog  required as feature prerequisites.
        requestExpirationReminderFeature = Preferences.FEATURE_EXPIRATION_REMINDERS.registerFeatureLauncher(
                this, this, options
        );

        mLoggedAccount.observe(this, resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    // we have just authenticated
                    LoggedAccount mLoggedAccount = resource.getData();
                    Log.d(TAG, "Success auth for account " + mLoggedAccount);

                    if( options.getBoolean(Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_ENABLED,
                            Preferences.FEATURE_EXPIRATION_REMINDERS.DEFAULT )) {
                        Preferences.FEATURE_EXPIRATION_REMINDERS.requestFeature(
                                requestExpirationReminderFeature, this, options
                        );
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

        // update toolbar data when requested by any destination through arguments
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

            String subtitle = null;
            if( arguments != null ) {
                subtitle = arguments.getString("subtitle");
            }

            if( subtitle != null ) {
                getSupportActionBar().setSubtitle(subtitle);
            }
            else {
                getSupportActionBar().setSubtitle(null);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if( Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_ENABLED.equals(key) ) {
            boolean isEnabled = sharedPreferences.getBoolean(key, Preferences.FEATURE_EXPIRATION_REMINDERS.DEFAULT);
            Log.d(TAG, "Changed " + isEnabled );

            boolean gotPreRequisites = true;
            if (isEnabled) {
                // request permissions required by this feature if needed
                gotPreRequisites = Preferences.FEATURE_EXPIRATION_REMINDERS.requestFeature(
                        requestExpirationReminderFeature, this, sharedPreferences
                );
            }

            if( gotPreRequisites ) {
                LiveData<Resource<LoggedAccount>> onEnable = featuresViewModel.enableFeatureExpirationReminders(isEnabled);
                onEnable.observe(this, new Observer<Resource<LoggedAccount>>() {

                    @Override
                    public void onChanged(Resource<LoggedAccount> resource) {
                        if( resource.getStatus() != Status.LOADING ) {
                            onEnable.removeObserver(this);
                        }
                    }
                });
            }
        }
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
        SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(this);
        options.unregisterOnSharedPreferenceChangeListener(this);

        this.mAppBarConfiguration = null;
        this.requestExpirationReminderFeature = null;
        this.mLoggedAccount.removeObservers(this);
        this.mLoggedAccount = null;

        super.onDestroy();
    }
}