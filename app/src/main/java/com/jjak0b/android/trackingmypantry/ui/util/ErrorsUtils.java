package com.jjak0b.android.trackingmypantry.ui.util;

import android.content.Context;
import android.util.Log;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.AuthException;
import com.jjak0b.android.trackingmypantry.data.api.NotLoggedInException;
import com.jjak0b.android.trackingmypantry.data.api.RemoteException;

import java.io.IOException;

public class ErrorsUtils {

    public static String getErrorMessage(final Context context, Throwable error, String debugTag) {
        String errorMsg;

        if( error instanceof NotLoggedInException ) {
            Log.e( debugTag, "Authentication Error", error );
            errorMsg = context.getString(R.string.error_msg_type_cause,
                    context.getString(R.string.error_type_authentication),
                    context.getString(R.string.error_cause_not_logged)
            );
        }
        else if( error instanceof AuthException){
            Log.e( debugTag, "Authentication Error", error );
            errorMsg = context.getString(R.string.error_msg_type_cause,
                    context.getString(R.string.error_type_authentication),
                    context.getString(R.string.signIn_failed)
            );
        }
        else if( error instanceof RemoteException){
            Log.e( debugTag, "Server Error", error );
            errorMsg = context.getString(R.string.error_msg_type_cause,
                    context.getString(R.string.error_type_remote),
                    context.getString(R.string.error_cause_bad_data)
            );
        }
        else if( error instanceof IOException){
            Log.e( debugTag, "Network Error", error );
            errorMsg = context.getString(R.string.error_msg_type_cause,
                    context.getString(R.string.error_type_network),
                    context.getString(R.string.error_cause_unable_communicate)
            );
        }
        else if( error instanceof FormException ) {
            errorMsg = error.getLocalizedMessage();
        }
        else {
            Log.e( debugTag, "Unexpected Error", error );
            errorMsg = context.getString(R.string.error_msg_type_cause,
                    context.getString(R.string.error_type_unhandled),
                    context.getString(R.string.error_cause_unable_do_operation)
            );
        }

        return errorMsg;
    }
}
