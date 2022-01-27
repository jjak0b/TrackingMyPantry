package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.IOBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.PurchaseInfoDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.db.relationships.PlaceWithPurchases;

import java.util.List;

public class PurchasesRepository {
    private static PurchasesRepository instance;
    private static final Object sInstanceLock = new Object();

    private AuthRepository authRepository;
    private AppExecutors mAppExecutors;
    private PantryDB pantryDB;
    private PurchaseInfoDao dao;

    private PurchasesRepository(final Context context) {
        authRepository = AuthRepository.getInstance(context);
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

    public LiveData<Resource<PurchaseInfo>> add(@NonNull PurchaseInfo info ) {
        return Transformations.forwardOnce(authRepository.getLoggedAccount(), rUser -> {
            String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
            info.setUserId(ownerID);
            return Transformations.forward(
                    Transformations.simulateApi(
                            mAppExecutors.diskIO(),
                            mAppExecutors.mainThread(),
                            () -> dao.insertPurchaseInfo(info)
                    ),
                    resourceID -> get(resourceID.getData())
            );
        });
    }

    public LiveData<Resource<PurchaseInfo>> get(@NonNull final long purchase_id ) {
        return IOBoundResource.adapt(mAppExecutors, dao.getPurchaseInfo(purchase_id));
    }

    public LiveData<Resource<List<PlaceWithPurchases>>> getAllPurchasePlacesOf(@NonNull String productID) {
        return IOBoundResource.adapt(mAppExecutors, dao.getAllPurchaseInfo(productID));
    }
}
