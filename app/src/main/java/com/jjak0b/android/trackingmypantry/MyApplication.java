package com.jjak0b.android.trackingmypantry;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.ResourceOptions;
import com.mapbox.maps.ResourceOptionsManager;
import com.mapbox.maps.TileStoreUsageMode;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.location.DefaultLocationProvider;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA );
            String mApiKey = appInfo.metaData.getString("com.mapbox.API_KEY");
            Log.e("MyApp", "MapBox token:" + mApiKey );

            MapboxSearchSdk.initialize(
                    this,
                    mApiKey,
                    new DefaultLocationProvider(this)
            );
            // Required for old plugins: search plugin
            Mapbox.getInstance(this, mApiKey);
            // Required for v10 api
            ResourceOptionsManager.Companion
                    .getDefault(this, mApiKey);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
}
