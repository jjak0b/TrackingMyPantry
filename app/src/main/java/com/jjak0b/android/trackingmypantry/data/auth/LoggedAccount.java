package com.jjak0b.android.trackingmypantry.data.auth;

import android.accounts.Account;

import org.jetbrains.annotations.NotNull;

public class LoggedAccount {
    Account account;

    public LoggedAccount(@NotNull Account account ) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public String getName() {
        return account.name;
    }
}
