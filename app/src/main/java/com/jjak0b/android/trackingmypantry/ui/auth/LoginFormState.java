package com.jjak0b.android.trackingmypantry.ui.auth;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class LoginFormState {

    @Nullable
    private Integer usernameError;
    @Nullable
    private Integer emailError;
    @Nullable
    private Integer passwordError;

    private boolean isDataValid;

    LoginFormState(@Nullable Integer emailError, @Nullable Integer passwordError, @Nullable Integer usernameError ) {
        this.emailError = emailError;
        this.passwordError = passwordError;
        this.usernameError = usernameError;
        this.isDataValid = false;
    }

    LoginFormState(boolean isDataValid) {
        this.emailError = null;
        this.usernameError = null;
        this.passwordError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getUsernameError() { return usernameError; }

    @Nullable
    Integer getEmailError() { return emailError; }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    public void setUsernameError(@Nullable Integer usernameError) {
        this.usernameError = usernameError;
        updateIsDataValid();
    }

    public void setEmailError(@Nullable Integer emailError) {
        this.emailError = emailError;
        updateIsDataValid();
    }

    public void setPasswordError(@Nullable Integer passwordError) {
        this.passwordError = passwordError;
        updateIsDataValid();
    }

    private void updateIsDataValid() {
        this.isDataValid = this.usernameError == null && this.emailError == null && this.passwordError == null;
    }


    boolean isDataValid() {
        return isDataValid;
    }
}