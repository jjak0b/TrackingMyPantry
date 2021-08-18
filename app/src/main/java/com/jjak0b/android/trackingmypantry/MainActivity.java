package com.jjak0b.android.trackingmypantry;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.auth.NotLoggedInException;
import com.jjak0b.android.trackingmypantry.services.Authenticator;
import com.jjak0b.android.trackingmypantry.services.ProductExpirationNotificationService;
import com.jjak0b.android.trackingmypantry.ui.auth.AuthViewModel;
import com.jjak0b.android.trackingmypantry.data.auth.LoginResult;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class MainActivity extends AppCompatActivity  {

    private AppBarConfiguration mAppBarConfiguration;
    private AuthViewModel authViewModel;

    private ActivityResultLauncher<Intent> chooseAccountLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_auth, R.id.nav_pantries, R.id.nav_gallery, R.id.nav_slideshow
        )
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        authViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory
                        .getInstance(getApplication())
        ).get(AuthViewModel.class);

        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog.
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your
                        // app.
                        startService(new Intent(this, ProductExpirationNotificationService.class));
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // features requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.
                    }
                });
        // Register the account chooser callback, which handles the user's response to the
        // system account dialog.
        chooseAccountLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        Bundle b = intent.getExtras();
                        String accountName = b.getString(AccountManager.KEY_ACCOUNT_NAME);

                        Log.d("Main", "setting account " + accountName );
                        authViewModel.setLoggedAccount( accountName );
                    }
                    else {
                        Toast.makeText(getBaseContext(), R.string.error_account_required, Toast.LENGTH_SHORT )
                                .show();
                        finish();
                    }
                });

        authViewModel.getLoginUIResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(LoginResult loginResult) {
                if( loginResult.getError() != null ){
                    Toast.makeText(getApplicationContext(),
                            getString(loginResult.getError(), R.string.to_authenticate),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });

        authenticate();


/*
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            startService(new Intent(this, ProductExpirationNotificationService.class));
        }
        else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_CALENDAR);
        }*/

    }


    private ListenableFuture<String> authenticate() {
        ListenableFuture<String> future = authViewModel.authenticate();
        Futures.addCallback(
                future,
                new FutureCallback<String>() {
                    @Override
                    public void onSuccess(@NullableDecl String result) {

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if( t instanceof NotLoggedInException ){
                            launchAccountChooser();
                        }
                        else {
                            authViewModel.setUIErrorFor( t, true);
                        }
                    }
                },
                ContextCompat.getMainExecutor(this)
        );
        return future;
    }

    private void launchAccountChooser() {
        Intent intent = AccountManager
                .newChooseAccountIntent(
                        null,
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
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

}