package com.jjak0b.android.trackingmypantry.data;

public class Preferences {
    public final static String KEY = null;
    public static class FEATURE_EXPIRATION_REMINDERS {
        public final static String KEY = "feature_expiration_reminders";
        public final static String KEY_ENABLED = "enabled_feature_expiration_reminders";
        public final static String KEY_DAYS_BEFORE = "feature_expiration_reminders_days_before";
        public final static String KEY_TIME = "feature_expiration_reminders_time";
        public final static int DEFAULT = -1;
        public final static int ENABLED = 1;
        public final static int DISABLED = 0;
        public final static int DAYS_BEFORE_DEFAULT = 2;
        public final static String TIME_DEFAULT = "12:00";
    }


}
