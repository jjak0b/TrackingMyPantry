package com.jjak0b.android.trackingmypantry.ui.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.jjak0b.android.trackingmypantry.R;

public class Permissions {

    public static boolean startFeaturesRequests(
            @NonNull Context context,
            @NonNull ActivityResultLauncher<String[]> requestPermissionLauncher,
            @NonNull final String[] PERMISSIONS,
            @StringRes int resRationaleRequestMessage,
            @StringRes int resMessageOnDenied
    ) {
        boolean hasPermissions = true;
        for (String permission : PERMISSIONS ) {
            if( ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                hasPermissions = false;
                break;
            }
        }

        if( !hasPermissions ) {
            showRationaleUI(
                    context,
                    resRationaleRequestMessage,
                    resMessageOnDenied,
                    (dialog, which) -> requestPermissionLauncher.launch(PERMISSIONS)
            );
        }
        return hasPermissions;
    }

    private static AlertDialog showRationaleUI(Context context, @StringRes int msg, @StringRes int msgOnCancel, DialogInterface.OnClickListener onOk) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.rationale_title_feature)
                .setMessage(msg)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    new AlertDialog.Builder(context)
                            .setPositiveButton(android.R.string.ok, null)
                            .setTitle(R.string.rationale_title_feature)
                            .setMessage(msgOnCancel)
                            .show();
                })
                .setPositiveButton(android.R.string.ok, onOk )
                .setCancelable(true)
                .show();
    }
}
