package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.data.db.results.ExpirationInfo;
import com.jjak0b.android.trackingmypantry.services.ExpireDateSyncAdapter;

public class ExpirationEventsRepository {
    private static final String TAG = "ExpirationEventsRepo";

    private static ExpirationEventsRepository instance;
    private static final Object sInstanceLock = new Object();
    private AuthRepository authRepo;
    private static final LiveData<Resource<Void>> voidRes = new MutableLiveData<>(Resource.success(null));

    private ExpirationEventsRepository(Context context) {
        authRepo = AuthRepository.getInstance(context);
    }

    public static ExpirationEventsRepository getInstance(Context context) {
        ExpirationEventsRepository i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new ExpirationEventsRepository(context);
                    i = instance;
                }
            }
        }
        return i;
    }

    /**
     * Will request a Sync to the {@link CalendarContract#AUTHORITY} to sync the events
     * matching with the info provided. if More info are provided then more the updates will be done more strictly to that data
     * @param info
     * @return
     */
    private LiveData<Resource<Void>> notify(@Nullable ExpirationInfo.Dummy info) {
        return Transformations.forwardOnce(authRepo.getLoggedAccount(), resource -> {
            LoggedAccount account = resource.getData();
            Bundle args = new Bundle();
            if( info != null) {
                args.putLong(ExpireDateSyncAdapter.EXTRA_EVENT_PANTRY_ID, info.getPantryID());
                args.putString(ExpireDateSyncAdapter.EXTRA_EVENT_PRODUCT_ID, info.getProductID());
                if( info.getExpireDate()  != null )
                    args.putLong(ExpireDateSyncAdapter.EXTRA_EVENT_DATE, info.getExpireDate().getTime() );
            }
            Log.d(TAG, "Requesting sync" );
            ContentResolver.requestSync(account.getAccount(), CalendarContract.AUTHORITY, args);
            return voidRes;
        });
    }

    /**
     * @see #notify(ExpirationInfo.Dummy)
     * @param info
     * @return
     */
    LiveData<Resource<Void>> insertExpiration(@Nullable ExpirationInfo.Dummy info) {
        return notify(info);
    }

    LiveData<Resource<Void>> updateExpiration(@Nullable ExpirationInfo.Dummy info) {
        return notify(info);
    }

    LiveData<Resource<Void>> removeExpiration(@Nullable ExpirationInfo.Dummy info) {
        return notify(info);
    }
}
