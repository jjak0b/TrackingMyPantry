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
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.db.results.ExpirationInfo;
import com.jjak0b.android.trackingmypantry.data.db.results.PantryDetails;
import com.jjak0b.android.trackingmypantry.util.ResourceUtils;

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

        LiveData<Resource<Pantry>> mPantrySearchSource = Pantry.isDummy(pantry) && pantry.getName() != null
                 ? searchPantry(pantry.getName()) // search for existing matching
                 : getPantry(pantry.getId()); // search for

        return Transformations.forwardOnce(mPantrySearchSource, resource -> {

            if( resource.getData() != null && !Pantry.isDummy(resource.getData())) {
                // get direct pantry live data
                return getPantry(resource.getData().getId());
            }
            else {
                // add pantry and get its live data
                return Transformations.forwardOnce(authRepo.getLoggedAccount(), rUser -> {
                    String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
                    pantry.setUserId(ownerID);
                    return Transformations.forwardOnce(
                            Transformations.simulateApi(
                                    mAppExecutors.diskIO(),
                                    mAppExecutors.mainThread(),
                                    () -> {
                                        return pantryDao.insert(pantry);
                                    }
                            ),
                            resourceID -> getPantry(resourceID.getData())
                    );
                });
            }
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
        return Transformations.forwardOnce(authRepo.getLoggedAccount(), rUser -> {
            String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
            pantry.setUserId(ownerID);
            return Transformations.simulateApi(
                    mAppExecutors.diskIO(),
                    mAppExecutors.mainThread(),
                    () -> pantryDao.update(pantry)
            );
        });
    }

    public LiveData<Resource<Pantry>> searchPantry(String name) {
        return Transformations.forward(authRepo.getLoggedAccount(), rUser -> {
            String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
            return IOBoundResource.adapt(mAppExecutors, pantryDao.get(name, ownerID));
        });
    }
    public LiveData<Resource<Pantry>> getPantry(long pantry_id ) {
        return Transformations.forward(authRepo.getLoggedAccount(), rUser -> {
            String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
            return IOBoundResource.adapt(mAppExecutors, pantryDao.get(pantry_id, ownerID));
        });
    }

    public LiveData<Resource<List<Pantry>>> getPantries(){
        return Transformations.forward(authRepo.getLoggedAccount(), rUser -> {
            String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
            return IOBoundResource.adapt(mAppExecutors, pantryDao.getAll(ownerID));
        });
    }

    public LiveData<Resource<ProductInstanceGroup>> getGroup(long group_id) {
        return IOBoundResource.adapt(mAppExecutors, groupDao.getGroup(group_id));
    }

    public LiveData<Resource<List<PantryDetails>>> getAllContaining(String productID ){
        return Transformations.forwardOnce(authRepo.getLoggedAccount(), rUser -> {
            String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
            return IOBoundResource.adapt(mAppExecutors, pantryDao.getAllWithGroupsContaining(ownerID, productID));
        });
    }

    public LiveData<Resource<List<ProductInstanceGroup>>> getContent(String product_id, long pantry_id) {
        return IOBoundResource.adapt(mAppExecutors, pantryDao.getContent(product_id, pantry_id));
    }

    public LiveData<Resource<Long>> addGroup(@NonNull ProductInstanceGroup instanceGroup, @Nullable UserProduct product, @NonNull Pantry pantry ) {
        if( product != null ) {
            instanceGroup.setProductId(product.getBarcode());
        }

        final MediatorLiveData<Resource<Long>> result = new MediatorLiveData<>();

        // require pantry to exists
        final LiveData<Resource<Long>> onInsert = Transformations.forwardOnce(add(pantry), rPantry -> {
            Pantry mPantry = rPantry.getData();
            instanceGroup.setPantryId(mPantry.getId());
            return Transformations.forwardOnce(authRepo.getLoggedAccount(), rUser -> {
                String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
                instanceGroup.setUserId(ownerID);
                return Transformations.simulateApi(
                        mAppExecutors.diskIO(),
                        mAppExecutors.mainThread(),
                        () -> groupDao.merge(instanceGroup)
                );
            });
        });

        result.addSource(onInsert, resource -> {
            if( resource.getStatus() == Status.SUCCESS ) {
                // observe change event
                LiveData<Resource<Void>> onEventChange = expireEventsRepo.insertExpiration(
                        new ExpirationInfo.Dummy()
                            .setPantryID(instanceGroup.getPantryId())
                            .setProductID(instanceGroup.getProductId())
                            .setExpireDate(instanceGroup.getExpiryDate())
                            .setQuantity(instanceGroup.getQuantity())
                );
                result.addSource(onEventChange, resource1 -> {
                    if( resource1.getStatus() != Status.LOADING ) result.removeSource(onEventChange);
                });
                // detach this and attach normal flow
                result.removeSource(onInsert);
                result.addSource(onInsert, result::setValue );
            }
            else {
                result.setValue(resource);
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> deleteGroup(@NonNull ProductInstanceGroup entry) {
        final MediatorLiveData<Resource<Void>> result = new MediatorLiveData<>();
        LiveData<Resource<Void>> onDelete = Transformations.simulateApi(
                mAppExecutors.diskIO(),
                mAppExecutors.mainThread(),
                () -> { groupDao.deleteAll(entry); return null; }
        );

        result.addSource(onDelete, resource -> {
            if( resource.getStatus() == Status.SUCCESS ) {
                // observe change event
                LiveData<Resource<Void>> onEventChange = expireEventsRepo.removeExpiration(
                        new ExpirationInfo.Dummy()
                                .setPantryID(entry.getPantryId())
                                .setProductID(entry.getProductId())
                                .setExpireDate(entry.getExpiryDate())
                                .setQuantity(entry.getQuantity())
                );
                result.addSource(onEventChange, resource1 -> {
                    if( resource1.getStatus() != Status.LOADING ) result.removeSource(onEventChange);
                });
                // detach this and attach normal flow
                result.removeSource(onDelete);
                result.addSource(onDelete, result::setValue );
            }
            else {
                result.setValue(resource);
            }
        });

        return result;
    }

    public LiveData<Resource<Integer>> updateGroup(@NonNull ProductInstanceGroup entry) {
        final MediatorLiveData<Resource<Integer>> result = new MediatorLiveData<>();
        LiveData<Resource<Integer>> onUpdate = Transformations.simulateApi(
                mAppExecutors.diskIO(),
                mAppExecutors.mainThread(),
                () -> groupDao.updateAll(entry)
        );

        result.addSource(onUpdate, resource -> {
            if( resource.getStatus() == Status.SUCCESS ) {
                // observe change event
                LiveData<Resource<Void>> onEventChange = expireEventsRepo.updateExpiration(
                        new ExpirationInfo.Dummy(entry)
                                .setPantryID(0)
                );
                result.addSource(onEventChange, resource1 -> {
                    if( resource1.getStatus() != Status.LOADING ) result.removeSource(onEventChange);
                });
                // detach this and attach normal flow
                result.removeSource(onUpdate);
                result.addSource(onUpdate, result::setValue );
            }
            else {
                result.setValue(resource);
            }
        });

        return result;
    }

    public LiveData<Resource<Long>> updateAndMergeGroups(ProductInstanceGroup entry ){

        final MediatorLiveData<Resource<Long>> result = new MediatorLiveData<>();
        LiveData<Resource<Long>> onUpdate = Transformations.simulateApi(
                mAppExecutors.diskIO(),
                mAppExecutors.mainThread(),
                () -> groupDao.merge(entry)
        );

        result.addSource(onUpdate, resource -> {
            if( resource.getStatus() == Status.SUCCESS ) {
                // observe change event
                LiveData<Resource<Void>> onEventChange = expireEventsRepo.updateExpiration(
                        new ExpirationInfo.Dummy(entry)
                );
                result.addSource(onEventChange, resource1 -> {
                    if( resource1.getStatus() != Status.LOADING ) result.removeSource(onEventChange);
                });
                // detach this and attach normal flow
                result.removeSource(onUpdate);
                result.addSource(onUpdate, result::setValue );
            }
            else {
                result.setValue(resource);
            }
        });

        return result;
    }


    public LiveData<Resource<Long>> moveGroupToPantry( ProductInstanceGroup entry, Pantry pantry, int quantity ){

        if( quantity >= entry.getQuantity() ){
            entry.setPantryId(pantry.getId());
            return updateAndMergeGroups(entry);
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
                        groupDao.merge(entry);
                        return groupDao.merge(newGroup);
                    })
            );

            result.addSource(onMove, resource -> {
                if( resource.getStatus() == Status.SUCCESS ) {
                    // observe change event
                    ResourceUtils.ResourcePairLiveData<Void, Void> mPair
                            = ResourceUtils.ResourcePairLiveData.create(
                                expireEventsRepo.updateExpiration(
                                        new ExpirationInfo.Dummy(entry)
                                ),
                                expireEventsRepo.updateExpiration(
                                        new ExpirationInfo.Dummy(newGroup)
                                )
                            );
                    result.addSource(mPair, pair -> {
                        if( pair.first.getStatus() != Status.LOADING
                        && pair.second.getStatus() != Status.LOADING ) {
                            result.removeSource(mPair);
                        }
                    });

                    // detach this and attach normal flow
                    result.removeSource(onMove);
                    result.addSource(onMove, result::setValue );
                }
                else {
                    result.setValue(resource);
                }
            });
            return result;
        }
    }
}
