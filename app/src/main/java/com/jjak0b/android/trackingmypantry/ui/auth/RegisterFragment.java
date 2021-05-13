package com.jjak0b.android.trackingmypantry.ui.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjak0b.android.trackingmypantry.R;

public class RegisterFragment extends LoginFormFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_regiter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText usernameEditText = view.findViewById(R.id.username);
        final EditText emailEditText = view.findViewById(R.id.email);
        final EditText passwordEditText = view.findViewById(R.id.password);
        final Button submitButton = view.findViewById(R.id.btnSubmitRegister);
        final ProgressBar loadingProgressBar = view.findViewById(R.id.loading);


        InputFilter[] inputFilters = new InputFilter[]{ filterWhitespaces() };
        emailEditText.setFilters( inputFilters );
        passwordEditText.setFilters( inputFilters );
        usernameEditText.setFilters( inputFilters );

        registerFormStateUpdate( emailEditText, passwordEditText, submitButton, loadingProgressBar );
        registerFormFieldsFeedback( emailEditText, passwordEditText, submitButton, loadingProgressBar );
        registerFormResultsFeedback( emailEditText, passwordEditText, submitButton, loadingProgressBar );
        registerFormSubmit( usernameEditText, emailEditText, passwordEditText, submitButton, loadingProgressBar );

    }

    protected void registerFormSubmit(
            EditText usernameEditText,
            EditText emailEditText,
            EditText passwordEditText,
            Button submitButton,
            ProgressBar loadingProgressBar
    ) {

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    register(
                            loadingProgressBar,
                            usernameEditText.getText().toString(),
                            emailEditText.getText().toString(),
                            passwordEditText.getText().toString()
                    );
                }
                return false;
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(
                        loadingProgressBar,
                        usernameEditText.getText().toString(),
                        emailEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );
            }
        });
    }

    private void register( ProgressBar loadingProgressBar, String username, String email, String password ) {
        loadingProgressBar.setVisibility(View.VISIBLE);

        formViewModel.register(
                username,
                email,
                password
        ).thenAccept( aVoid -> {
            loadingProgressBar.setVisibility(View.GONE);
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome_register) + " " + model.getDisplayName();
        // TODO : initiate successful logged in experience

        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void registerFormResultsFeedback(EditText emailEditText, EditText passwordEditText, Button submitButton, ProgressBar loadingProgressBar) {
        super.registerFormResultsFeedback(emailEditText, passwordEditText, submitButton, loadingProgressBar);

        formViewModel.getLoginUIResult()
                .observe(getViewLifecycleOwner(), new Observer<LoginResult>() {
                    @Override
                    public void onChanged(LoginResult loginResult) {
                        Log.d("RegisterFragment", "LoginResult changed");

                        if (loginResult.getSuccess() != null) {
                            updateUiWithUser(loginResult.getSuccess());
                        }
                    }
                });
    }
}