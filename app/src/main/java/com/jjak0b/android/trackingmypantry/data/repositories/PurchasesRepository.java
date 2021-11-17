package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.PurchaseInfoDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;

public class PurchasesRepository {
    private static PurchasesRepository instance;
    private static final Object sInstanceLock = new Object();

    private AppExecutors mAppExecutors;
    private PantryDB pantryDB;
    private PurchaseInfoDao dao;

    private PurchasesRepository(final Context context) {
        pantryDB = PantryDB.getInstance( context );
        dao = pantryDB.getPurchaseInfoDao();
        mAppExecutors = AppExecutors.getInstance();
    }

    public static PurchasesRepository getInstance(Context context) {
        PurchasesRepository i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new PurchasesRepository(context);
                    i = instance;
                }
            }
        }
        return i;
    }

    public LiveData<Resource<PurchaseInfo>> add(@NonNull PurchaseInfo purchase ) {
        final MediatorLiveData<Resource<PurchaseInfo>> mResult = new MediatorLiveData<>();
        mResult.setValue(Resource.loading(null));

        mAppExecutors.diskIO().execute(() -> {
            try {
                long purchase_id = dao.insertPurchaseInfo(purchase);
                mResult.addSource(dao.getPurchaseInfo(purchase_id), placeResult -> {
                    mResult.postValue(Resource.success(placeResult));
                });
            }
            // forward any error to caller
            catch (Throwable t) {
                mResult.postValue(Resource.error(t, purchase));
            }
        });

        return mResult;
    }

    public LiveData<Resource<PurchaseInfo>> get(@NonNull long purchase_id ) {
        final MediatorLiveData<Resource<PurchaseInfo>> mResult = new MediatorLiveData<>();
        mResult.setValue(Resource.loading(null));

        mAppExecutors.diskIO().execute(() -> {
            try {
                mResult.addSource(dao.getPurchaseInfo(purchase_id), placeResult -> {
                    mResult.postValue(Resource.success(placeResult));
                });
            }
            // forward any error to caller
            catch (Throwable t) {
                mResult.postValue(Resource.error(t, null));
            }
        });

        return mResult;
    }
}
