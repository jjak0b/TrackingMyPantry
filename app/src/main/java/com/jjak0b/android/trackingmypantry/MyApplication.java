package com.jjak0b.android.trackingmypantry;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.mapbox.maps.ResourceOptionsManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA );
            String mApiKey = appInfo.metaData.getString("com.mapbox.API_KEY");

            // init access token here so we can use it later from singleton
            ResourceOptionsManager.Companion.getDefault(this, mApiKey);
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e("MyApp", "Unable to set MapBox token:", e );
            e.printStackTrace();
        }
    }
}
