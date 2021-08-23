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

    public static class FeatureRequestBuilder {
        Runnable onNegativeCallback;

        ActivityResultLauncher<String[]> requestPermissionLauncher;

        String[] PERMISSIONS;

        @StringRes
        int resMessageOnDenied;
        @StringRes
        int resRationaleRequestMessage;

        public FeatureRequestBuilder() { }

        public FeatureRequestBuilder setRationaleMessage(@StringRes int resRationaleRequestMessage ) {
            this.resRationaleRequestMessage = resRationaleRequestMessage;
            return this;
        }

        public FeatureRequestBuilder setOnPositive(@NonNull ActivityResultLauncher<String[]> requestPermissionLauncher, @NonNull final String[] permissions ) {
            this.requestPermissionLauncher = requestPermissionLauncher;
            this.PERMISSIONS = permissions;
            return this;
        }

        public FeatureRequestBuilder setOnNegative(@StringRes int resMessageOnDenied, Runnable onNegativeCallback ) {
            this.resMessageOnDenied = resMessageOnDenied;
            this.onNegativeCallback = onNegativeCallback;
            return this;
        }


        private AlertDialog showRationaleUI(Context context, @StringRes int msg, Runnable onOk, Runnable onCancel ) {
            return new AlertDialog.Builder(context)
                    .setTitle(R.string.rationale_title_feature)
                    .setMessage(msg)
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        if( onCancel != null ) onCancel.run();
                    })
                    .setPositiveButton(android.R.string.ok,  (dialog, which) -> {
                        if( onOk != null ) onOk.run();
                    })
                    .setCancelable(true)
                    .show();
        }

        public boolean show(@NonNull Context context) {
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
                        () -> requestPermissionLauncher.launch(PERMISSIONS),
                        () -> {
                            new AlertDialog.Builder(context)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setTitle(R.string.rationale_title_feature)
                                    .setMessage(resMessageOnDenied)
                                    .show()
                                    .setOnDismissListener(dialog1 -> {
                                        if( onNegativeCallback != null ) onNegativeCallback.run();
                                    });
                        }
                );
            }
            return hasPermissions;
        }
    }
}
