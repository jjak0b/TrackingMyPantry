package com.jjak0b.android.trackingmypantry.services;

import android.content.Intent;
import android.os.IBinder;

import androidx.lifecycle.LifecycleService;

/**
 * Define a Service that returns an <code><a href="/reference/android/os/IBinder.html">IBinder</a></code> for the
 * sync adapter class, allowing the sync adapter framework to call
 * onPerformSync().
 */
public class ExpireDateSyncService extends LifecycleService {
    // Storage for an instance of the sync adapter
    private static ExpireDateSyncAdapter sExpireDateSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        super.onCreate();
        synchronized (sSyncAdapterLock) {
            if (sExpireDateSyncAdapter == null) {
                sExpireDateSyncAdapter = new ExpireDateSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        super.onBind(intent);
        return sExpireDateSyncAdapter.getSyncAdapterBinder();
    }
}