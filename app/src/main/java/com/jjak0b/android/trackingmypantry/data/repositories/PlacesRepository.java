package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
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
        final MediatorLiveData<Resource<Place>> mResult = new MediatorLiveData<>();
        mResult.setValue(Resource.loading(null));

        mAppExecutors.diskIO().execute(() -> {
            try {
                dao.insertPlace(place);
                mResult.addSource(dao.getPlace(place.getId()), placeResult -> {
                    mResult.postValue(Resource.success(placeResult));
                });
            }
            // forward any error to caller
            catch (Throwable t) {
                mResult.postValue(Resource.error(t, place));
            }
        });

        return mResult;
    }

    public LiveData<Resource<Place>> get(@NonNull String place_id ) {
        final MediatorLiveData<Resource<Place>> mResult = new MediatorLiveData<>();
        mResult.setValue(Resource.loading(null));

        mAppExecutors.diskIO().execute(() -> {
            try {
                mResult.addSource(dao.getPlace(place_id), placeResult -> {
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
