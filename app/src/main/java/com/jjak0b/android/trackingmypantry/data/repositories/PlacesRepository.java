package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.IOBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.PlaceDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;

public class PlacesRepository {
    private static PlacesRepository instance;
    private static final Object sInstanceLock = new Object();

    private AppExecutors mAppExecutors;
    private PantryDB pantryDB;
    private PlaceDao dao;

    private PlacesRepository(final Context context) {
        pantryDB = PantryDB.getInstance( context );
        dao = pantryDB.getPlaceDao();
        mAppExecutors = AppExecutors.getInstance();
    }

    public static PlacesRepository getInstance(Context context) {
        PlacesRepository i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new PlacesRepository(context);
                    i = instance;
                }
            }
        }
        return i;
    }

    public LiveData<Resource<Place>> add(@NonNull Place place ) {
        return Transformations.forward(
                Transformations.simulateApi(
                        mAppExecutors.diskIO(),
                        mAppExecutors.mainThread(),
                        () -> {
                            dao.insertPlace(place);return place.getId();
                        }
                ),
                resourceID -> get(resourceID.getData())
        );
    }

    public LiveData<Resource<Place>> get(@NonNull final String place_id ) {
        return IOBoundResource.adapt(mAppExecutors, dao.getPlace(place_id));
    }
}
