package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.IOBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.PantryDao;
import com.jjak0b.android.trackingmypantry.data.db.daos.ProductInstanceDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.results.PantryDetails;

import java.util.List;

public class PantriesRepository {
    private static PantriesRepository instance;
    private static final Object sInstanceLock = new Object();

    private static final String TAG = "PantryRepo";

    private AuthRepository authRepo;
    private ExpirationEventsRepository expireEventsRepo;
    private PantryDB pantryDB;
    private PantryDao pantryDao;
    private ProductInstanceDao groupDao;
    private AppExecutors mAppExecutors;

    private LiveData<Resource<Pantry>> mDefaultPantry;

    private PantriesRepository(final Context context) {
        authRepo = AuthRepository.getInstance(context);
        expireEventsRepo = ExpirationEventsRepository.getInstance(context);
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

    public LiveData<Resource<Void>> remove(@NonNull Pantry pantry) {
        return Transformations.simulateApi(
                mAppExecutors.diskIO(),
                mAppExecutors.mainThread(),
                () -> { pantryDao.delete(pantry); return null; }
        );
    }

    public LiveData<Resource<Integer>> update(@NonNull Pantry pantry) {
        return Transformations.forwardOnce(authRepo.getLoggedAccount(), resourceAccount -> {
            pantry.setUserId(resourceAccount.getData().getId());
            return Transformations.simulateApi(
                    mAppExecutors.diskIO(),
                    mAppExecutors.mainThread(),
                    () -> pantryDao.update(pantry)
            );
        });
    }

    public LiveData<Resource<Pantry>> searchPantry(String name) {
        return Transformations.forward(authRepo.getLoggedAccount(), resource -> {
            return IOBoundResource.adapt(mAppExecutors, pantryDao.get(name, resource.getData().getId()));
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

    public LiveData<Resource<List<PantryDetails>>> getAllContaining(String productID ){
        return IOBoundResource.adapt(mAppExecutors, pantryDao.getAllWithGroupsContaining(productID));
    }

    public LiveData<Resource<List<ProductInstanceGroup>>> getContent(String product_id, long pantry_id) {
        return IOBoundResource.adapt(mAppExecutors, pantryDao.getContent(product_id, pantry_id));
    }

    public LiveData<Resource<Long>> addGroup(@NonNull ProductInstanceGroup instanceGroup, @Nullable Product product, @NonNull Pantry pantry ) {
        instanceGroup.setPantryId(pantry.getId());
        if( product != null ) {
            instanceGroup.setProductId(product.getBarcode());
        }

        final MediatorLiveData<Resource<Long>> result = new MediatorLiveData<>();
        final LiveData<Resource<Long>> onInsert = Transformations.simulateApi(
                mAppExecutors.diskIO(),
                mAppExecutors.mainThread(),
                () -> groupDao.mergeInsert(instanceGroup)
        );

        result.addSource(onInsert, resource -> {
            if( resource.getStatus() == Status.SUCCESS ) {
                expireEventsRepo.insertExpiration(resource.getData());
                result.removeSource(onInsert);

                result.addSource(onInsert, result::setValue );
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> deleteGroups(@NonNull ProductInstanceGroup... entries) {
        final MediatorLiveData<Resource<Void>> result = new MediatorLiveData<>();
        LiveData<Resource<Void>> onDelete = Transformations.simulateApi(
                mAppExecutors.diskIO(),
                mAppExecutors.mainThread(),
                () -> { groupDao.deleteAll(entries); return null; }
        );

        result.addSource(onDelete, resource -> {
            if( resource.getStatus() == Status.SUCCESS ) {
                for (ProductInstanceGroup group : entries) {
                    expireEventsRepo.removeExpiration(group.getId());
                }
                result.removeSource(onDelete);
                result.addSource(onDelete, result::setValue);
            }
        });

        return result;
    }

    public LiveData<Resource<Integer>> updateGroups(@NonNull ProductInstanceGroup... entries) {
        final MediatorLiveData<Resource<Integer>> result = new MediatorLiveData<>();
        LiveData<Resource<Integer>> onUpdate = Transformations.simulateApi(
                mAppExecutors.diskIO(),
                mAppExecutors.mainThread(),
                () -> groupDao.updateAll(entries)
        );

        result.addSource(onUpdate, resource -> {
            if( resource.getStatus() == Status.SUCCESS ) {
                for (ProductInstanceGroup entry : entries) {
                    expireEventsRepo.updateExpiration(null, null, entry.getId());
                }
                result.removeSource(onUpdate);
                result.addSource(onUpdate, result::setValue);
            }
        });

        return result;
    }

    public LiveData<Resource<Integer>> updateAndMergeGroups(ProductInstanceGroup entry ){

        final MediatorLiveData<Resource<Integer>> result = new MediatorLiveData<>();
        LiveData<Resource<Integer>> onDelete = Transformations.simulateApi(
                mAppExecutors.diskIO(),
                mAppExecutors.mainThread(),
                () -> groupDao.mergeUpdate(entry)
        );

        result.addSource(onDelete, resource -> {
            if( resource.getStatus() == Status.SUCCESS ) {
                expireEventsRepo.updateExpiration(null, null, entry.getId());
                result.removeSource(onDelete);
                result.addSource(onDelete, result::setValue);
            }
        });

        return result;
    }


    public LiveData<Resource<Long>> moveGroupToPantry( ProductInstanceGroup entry, Pantry pantry, int quantity ){

        if( quantity >= entry.getQuantity() ){
            entry.setPantryId(pantry.getId());
            // convert output to a Long
            return androidx.lifecycle.Transformations.map(updateAndMergeGroups(entry), input -> {
                Long value = input.getData() != null ? Long.valueOf(input.getData()) : null;
                switch (input.getStatus()) {
                    case SUCCESS:
                        return Resource.success(value);
                    case ERROR:
                        return Resource.error(input.getError(), value);
                    default:
                        return Resource.loading(value);
                }
            });
        }
        else {
            final MediatorLiveData<Resource<Long>> result = new MediatorLiveData<>();
            ProductInstanceGroup newGroup = ProductInstanceGroup.from(entry);
            newGroup.setId(0);
            newGroup.setPantryId(pantry.getId());
            newGroup.setQuantity(quantity);
            entry.setQuantity(entry.getQuantity() - quantity);

            final LiveData<Resource<Long>> onMove = Transformations.simulateApi(
                    mAppExecutors.diskIO(),
                    mAppExecutors.mainThread(),
                    () -> pantryDB.runInTransaction( () -> {
                        groupDao.mergeUpdate(entry);
                        return groupDao.mergeInsert(newGroup);
                    })
            );

            result.addSource(onMove, resource -> {
                if( resource.getStatus() == Status.SUCCESS ) {
                    expireEventsRepo.insertExpiration(resource.getData());
                    result.removeSource(onMove);

                    result.addSource(onMove, result::setValue );
                }
            });
            return result;
        }
    }
}
