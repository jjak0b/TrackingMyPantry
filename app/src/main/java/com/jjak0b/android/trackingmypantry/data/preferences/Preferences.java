package com.jjak0b.android.trackingmypantry.data.preferences;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.util.Permissions;

public class Preferences {
    public static class FEATURE_EXPIRATION_REMINDERS {
        public final static String KEY = "feature_expiration_reminders";
        public final static String KEY_ENABLED = "enabled_feature_expiration_reminders";
        public final static String KEY_DAYS_BEFORE = "feature_expiration_reminders_days_before";
        public final static String KEY_TIME = "feature_expiration_reminders_time";
        public final static boolean ENABLED = true;
        public final static boolean DISABLED = false;
        public final static boolean DEFAULT = ENABLED;
        public final static int DAYS_BEFORE_DEFAULT = 2;
        public final static String TIME_DEFAULT = "12:00";

        /**
         * Request to enable the feature, prompting user to permissions wizard,
         * The callback is handled by {@link #registerFeatureLauncher(ActivityResultCaller, Context, SharedPreferences)}
         * @param launcher
         * @param context
         * @param options
         * @return true if we have prerequisites to enable it, false if requesting prerequisites
         */
        public static boolean requestFeature(ActivityResultLauncher<String[]> launcher, @NonNull Context context, @NonNull SharedPreferences options) {
            return new Permissions.FeatureRequestBuilder()
                    .setRationaleMessage(R.string.rationale_msg_features_calendar)
                    .setOnPositive(launcher, new String[] {
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR
                    })
                    .setOnNegative(R.string.features_calendar_disabled, () -> {
                        options.edit()
                                .putBoolean(Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_ENABLED, Preferences.FEATURE_EXPIRATION_REMINDERS.DISABLED)
                                .apply();
                    })
                    .show(context);

        }

        public static ActivityResultLauncher<String[]> registerFeatureLauncher(@NonNull ActivityResultCaller caller, @NonNull Context context, @NonNull SharedPreferences options) {
            return caller.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                boolean areAllGranted = !isGranted.containsValue(false);
                options.edit().putBoolean(
                        Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_ENABLED,
                        areAllGranted
                                ? Preferences.FEATURE_EXPIRATION_REMINDERS.ENABLED
                                : Preferences.FEATURE_EXPIRATION_REMINDERS.DISABLED
                ).apply();
                if( !areAllGranted ) {

                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied.
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.rationale_title_feature)
                            .setMessage(R.string.features_calendar_disabled_cause_permissions)
                            .setPositiveButton(android.R.string.ok, null)
                            .setCancelable(false)
                            .show();
                }
            });
        }

    }


}
