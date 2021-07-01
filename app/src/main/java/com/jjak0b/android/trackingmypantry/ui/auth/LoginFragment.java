package com.jjak0b.android.trackingmypantry.ui.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
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
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

public class LoginFragment extends LoginFormFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText emailEditText = view.findViewById(R.id.email);
        final EditText passwordEditText = view.findViewById(R.id.password);
        final Button submitButton = view.findViewById(R.id.btnSubmitLogin);
        final ProgressBar loadingProgressBar = view.findViewById(R.id.loading);

        InputFilter[] inputFilters = new InputFilter[]{ InputUtil.filterWhitespaces() };
        emailEditText.setFilters( inputFilters );
        passwordEditText.setFilters( inputFilters );

        registerFormStateUpdate( emailEditText, passwordEditText, submitButton, loadingProgressBar );
        registerFormFieldsFeedback( emailEditText, passwordEditText, submitButton, loadingProgressBar );
        registerFormResultsFeedback( emailEditText, passwordEditText, submitButton, loadingProgressBar );
        registerFormSubmit( emailEditText, passwordEditText, submitButton, loadingProgressBar );
    }

    protected void registerFormSubmit(
            EditText emailEditText,
            EditText passwordEditText,
            Button submitButton,
            ProgressBar loadingProgressBar
    ) {

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ) {
                    login(
                            loadingProgressBar,
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
                login(
                        loadingProgressBar,
                        emailEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );
            }
        });
    }

    private void login(  ProgressBar loadingProgressBar, String email, String password ) {
        loadingProgressBar.setVisibility(View.VISIBLE);

        formViewModel.login(
                email,
                password
        ).thenAccept( aVoid -> {
            loadingProgressBar.setVisibility(View.GONE);
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome_login) + " " + model.getDisplayName();
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
                        Log.d("LoginFragment", "LoginResult changed");

                        if (loginResult.getSuccess() != null) {
                            updateUiWithUser(loginResult.getSuccess());
                        }
                    }
                });
    }

}