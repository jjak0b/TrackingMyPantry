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
import com.jjak0b.android.trackingmypantry.data.db.daos.ProductInstanceDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;

import java.util.List;

public class PantriesRepository {
    private static PantriesRepository instance;
    private static final Object sInstanceLock = new Object();

    private static final String TAG = "PantryRepo";

    private AuthRepository authRepo;
    private PantryDB pantryDB;
    private PantryDao pantryDao;
    private ProductInstanceDao groupDao;
    private AppExecutors mAppExecutors;

    private LiveData<Resource<Pantry>> mDefaultPantry;

    private PantriesRepository(final Context context) {
        authRepo = AuthRepository.getInstance(context);
        mAppExecutors = AppExecutors.getInstance();
        pantryDB = PantryDB.getInstance( context );
        pantryDao = pantryDB.getPantryDao();
        groupDao = pantryDB.getProductInstanceDao();
        mDefaultPantry = getPantry(1);
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
        return Transformations.forwardOnce(authRepo.getLoggedAccount(), resourceAccount -> {
            pantry.setUserId(resourceAccount.getData().getId());
            return Transformations.forwardOnce(
                    Transformations.simulateApi(
                            mAppExecutors.diskIO(),
                            mAppExecutors.mainThread(),
                            () -> {
                                if( Pantry.isDummy(pantry))
                                    return pantryDao.insert(pantry);
                                else
                                    return pantry.getId();
                            }
                    ),
                    resourceID -> getPantry(resourceID.getData())
            );
        });
    }

    public LiveData<Resource<Pantry>> getPantry(long pantry_id ) {
        return Transformations.forward(authRepo.getLoggedAccount(), resource -> {
            return IOBoundResource.adapt(mAppExecutors, pantryDao.get(pantry_id, resource.getData().getId()));
        });
    }

    public LiveData<Resource<List<Pantry>>> getPantries(){
        return Transformations.forward(authRepo.getLoggedAccount(), resource -> {
            return IOBoundResource.adapt(
                    mAppExecutors,
                    pantryDao.getAll(resource.getData().getId())
            );
        });
    }
    public LiveData<Resource<ProductInstanceGroup>> add(@NonNull ProductInstanceGroup group) {
        return Transformations.forward(
                Transformations.simulateApi(
                        mAppExecutors.diskIO(),
                        mAppExecutors.mainThread(),
                        () -> groupDao.mergeInsert(group)
                ),
                resourceID -> getGroup(resourceID.getData())
        );
    }

    public LiveData<Resource<ProductInstanceGroup>> getGroup(long group_id) {
        return IOBoundResource.adapt(mAppExecutors, groupDao.getGroup(group_id));
    }
}
