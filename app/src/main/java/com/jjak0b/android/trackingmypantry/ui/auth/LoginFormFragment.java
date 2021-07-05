package com.jjak0b.android.trackingmypantry.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.LoginCredentials;

public class LoginFormFragment extends Fragment {

    protected LoginViewModel formViewModel;


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

        formViewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory
                        .getInstance(getActivity().getApplication())
        ).get(LoginViewModel.class);

        /*
        Log.d( "LoginFormFragment", "removing observers");
        // debug
        formViewModel.getLoginResult().removeObservers( getViewLifecycleOwner() );
        formViewModel.getLoggedUser().removeObservers( getViewLifecycleOwner() );
        formViewModel.getLoginFormState().removeObservers( getViewLifecycleOwner() );
        Log.d( "LoginFormFragment", "registering test observers");

        formViewModel.getLoginResult().observe( getViewLifecycleOwner(), loginResult -> {
            Log.d( "LoginFormFragment", "Updated loginResult" );
        });
        formViewModel.getLoggedUser().observe( getViewLifecycleOwner(), loginResult -> {
            Log.d( "LoginFormFragment", "Updated logged user" );
        });
        formViewModel.getLoginFormState().observe( getViewLifecycleOwner(), loginResult -> {
            Log.d( "LoginFormFragment", "Updated form state" );
        });*/
    }

    protected void registerFormStateUpdate(
            EditText emailEditText,
            EditText passwordEditText,
            Button submitButton,
            ProgressBar loadingProgressBar
    ) {
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("LoginFormFragment", "->TextWatcher changed");
                formViewModel.updateFormState(
                        emailEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );
                Log.d("LoginFormFragment", "<-TextWatcher changed end");
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        LiveData<LoginCredentials> mAuth = formViewModel.getLoggedUser();

        mAuth.observe(getViewLifecycleOwner(), credentials -> {
            Log.d("LoginFormFragment", "LoggedUser changed " + credentials);
            emailEditText.setText( credentials.getEmail() );
            passwordEditText.setText( credentials.getPassword() );
        });
    }

    protected void registerFormResultsFeedback(
            EditText emailEditText,
            EditText passwordEditText,
            Button submitButton,
            ProgressBar loadingProgressBar
    ) {

        formViewModel.getLoginUIResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                Log.d("LoginFormFragment", "LoginResult changed" );
                if (loginResult == null) {
                    return;
                }

                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
            }
        });
    }

    protected void registerFormFieldsFeedback(
            EditText emailEditText,
            EditText passwordEditText,
            Button submitButton,
            ProgressBar loadingProgressBar
    ) {
        formViewModel.getLoginFormState().observe( this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                Log.d("LoginFormFragment", "LoginFormState changed " + (loginFormState == null) );
                if (loginFormState == null) {
                    return;
                }

                boolean isValid = loginFormState.isDataValid();

                submitButton.setEnabled( isValid );
                if (loginFormState.getEmailError() != null) {
                    Log.d("LoginFormFragment", "LoginFormState changed -> " + "set email error"  );
                    emailEditText.setError(getString(loginFormState.getEmailError()));
                }
                else {
                    emailEditText.setError( null );
                }

                if (loginFormState.getPasswordError() != null) {
                    Log.d("LoginFormFragment", "LoginFormState changed -> " + "set pass error"  );
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
                else {
                    passwordEditText.setError( null );
                }
            }
        });
    }

    protected void showLoginFailed(@StringRes Integer errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }
}