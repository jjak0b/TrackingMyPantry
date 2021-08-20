package com.jjak0b.android.trackingmypantry.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.lifecycle.Observer;

import com.jjak0b.android.trackingmypantry.data.auth.LoggedAccount;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.services.ExpireDateSyncAdapter;

public class ExpirationEventsRepository {
    private static ExpirationEventsRepository instance;
    private static final Object sInstanceLock = new Object();
    private LoginRepository authRepo;
    private ExpirationEventsRepository(Context context) {
        authRepo = LoginRepository.getInstance(context);
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

    void insertExpiration(Long productGroupID) {
        if( authRepo.isLoggedIn() ) {
            LoggedAccount account = authRepo.getLoggedInUser().getValue();
            Bundle args = new Bundle();
            /*args.putLong(ExpireDateSyncAdapter.EXTRA_OPERATION_EVENT, ExpireDateSyncAdapter.OPERATION_EVENT_INSERT);
            args.putLong(ExpireDateSyncAdapter.EXTRA_EVENT_GROUP_ID, productGroupID);*/
            Log.d("ExpirationEventsRepo", "requesting insert sync");
            ContentResolver.requestSync(account.getAccount(), CalendarContract.AUTHORITY, args);
            Log.d("ExpirationEventsRepo", "requested insert sync");
        }
    }

    void updateExpiration(String productID, Long pantryID, Long groupID) {
        Log.d("ExpirationEventsRepo", "requesting 1 update sync");
        if( authRepo.isLoggedIn() ) {
            Log.d("ExpirationEventsRepo", "requesting 2 update sync");
            LoggedAccount account = authRepo.getLoggedInUser().getValue();
            Log.d("ExpirationEventsRepo", "requesting 3 update sync");
            Bundle args = new Bundle();
            /*args.putLong(ExpireDateSyncAdapter.EXTRA_OPERATION_EVENT, ExpireDateSyncAdapter.OPERATION_EVENT_UPDATE);
            args.putLong(ExpireDateSyncAdapter.EXTRA_EVENT_GROUP_ID, groupID);
            args.putString(ExpireDateSyncAdapter.EXTRA_EVENT_PRODUCT_ID, productID);
            args.putLong(ExpireDateSyncAdapter.EXTRA_EVENT_PANTRY_ID, pantryID);*/

            Log.d("ExpirationEventsRepo", "requesting update sync");
            ContentResolver.requestSync(account.getAccount(), CalendarContract.AUTHORITY, args);
            Log.d("ExpirationEventsRepo", "requested update sync");
        }
        Log.d("ExpirationEventsRepo", "requesting 4 update sync");
    }

    void removeExpiration(Long productGroupID) {
        if( authRepo.isLoggedIn() ) {
            LoggedAccount account = authRepo.getLoggedInUser().getValue();
            Bundle args = new Bundle();
            /*args.putLong(ExpireDateSyncAdapter.EXTRA_OPERATION_EVENT, ExpireDateSyncAdapter.OPERATION_EVENT_REMOVE);
            args.putLong(ExpireDateSyncAdapter.EXTRA_EVENT_GROUP_ID, productGroupID);*/
            Log.d("ExpirationEventsRepo", "requesting remove sync");
            ContentResolver.requestSync(account.getAccount(), CalendarContract.AUTHORITY, args);
            Log.d("ExpirationEventsRepo", "requested remove sync");
        }
    }
}
