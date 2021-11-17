package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.IOBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.PantryDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;

public class PantriesRepository {
    private static PantriesRepository instance;
    private static final Object sInstanceLock = new Object();

    private static final String TAG = "PantryRepo";

    private AuthRepository authRepo;
    private PantryDB pantryDB;
    private PantryDao dao;
    private AppExecutors mAppExecutors;

    private LiveData<Resource<Pantry>> mDefaultPantry;

    private PantriesRepository(final Context context) {
        authRepo = AuthRepository.getInstance(context);
        mAppExecutors = AppExecutors.getInstance();
        pantryDB = PantryDB.getInstance( context );
        dao = pantryDB.getPantryDao();
        mDefaultPantry = IOBoundResource.adapt(mAppExecutors, dao.get(1) );
    }

    public static PantriesRepository getInstance(Context context) {
        PantriesRepository i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new PantriesRepository(context);
                    i = instance;
                }
            }
        }
        return i;
    }

    public LiveData<Resource<Pantry>> add(@NonNull Pantry pantry ) {
        return Transformations.forward(
                Transformations.simulateApi(
                        mAppExecutors.diskIO(),
                        mAppExecutors.mainThread(),
                        () -> dao.insert(pantry)
                ),
                resourceID -> getPantry(resourceID.getData())
        );
    }

    public LiveData<Resource<Pantry>> getPantry(@NonNull long pantry_id ) {
        return IOBoundResource.adapt(mAppExecutors, dao.get(pantry_id) );
    }
}
