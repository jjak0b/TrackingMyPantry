package com.jjak0b.android.trackingmypantry.ui.util;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class FormException extends Exception {
    @StringRes
    private int message;

    public FormException(String message) {
        super(message);
    }

    public FormException(@StringRes int message) {
        this.message = message;
    }

    @Nullable
    public String getLocalizedMessage(final Context context) {
        if( message != 0 ){
            return context.getString(message);
        }
        else {
            return super.getLocalizedMessage();
        }
    }
}
