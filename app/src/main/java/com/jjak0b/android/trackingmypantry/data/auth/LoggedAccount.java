package com.jjak0b.android.trackingmypantry.data.auth;

import android.accounts.Account;
import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.data.db.entities.User;

public class LoggedAccount extends User {
    Account account;

    private LoggedAccount(@NonNull Account account, @NonNull User user) {
        super(user);
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public String getName() {
        return account.name;
    }

    public String getEmail() {
        return getName();
    }

    public static class Builder {

        private Account account;
        private User user;

        public Builder() {
            this.account = null;
            this.user = null;
        }

        public Builder setAccount(@NonNull Account account) {
            this.account = account;
            return this;
        }

        public Builder setUser(@NonNull User user) {
            this.user = user;

            return this;
        }

        public LoggedAccount build() {
            if( account != null && user != null ){
                return new LoggedAccount(account, user);
            }
            else {
                throw new IllegalArgumentException("Logged account builder requires both account and user");
            }
        }
    }
}
