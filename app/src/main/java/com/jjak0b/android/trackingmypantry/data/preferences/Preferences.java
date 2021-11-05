package com.jjak0b.android.trackingmypantry.data.preferences;

public class Preferences {
    public static class FEATURE_EXPIRATION_REMINDERS {
        public final static String KEY = "feature_expiration_reminders";
        public final static String KEY_ENABLED = "enabled_feature_expiration_reminders";
        public final static String KEY_DAYS_BEFORE = "feature_expiration_reminders_days_before";
        public final static String KEY_TIME = "feature_expiration_reminders_time";
        public final static boolean ENABLED = true;
        public final static boolean DISABLED = false;
        public final static boolean DEFAULT = DISABLED;
        public final static int DAYS_BEFORE_DEFAULT = 2;
        public final static String TIME_DEFAULT = "12:00";
    }


}
