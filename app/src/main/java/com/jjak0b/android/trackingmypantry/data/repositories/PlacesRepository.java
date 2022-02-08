package com.jjak0b.android.trackingmypantry.data.repositories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.PlaceSearchSuggestion;
import com.jjak0b.android.trackingmypantry.data.PlaceSearchSuggestionBuilder;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.IOBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.NetworkBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.dataSource.PlacesDataSource;
import com.jjak0b.android.trackingmypantry.data.db.Converters;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.PlaceDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.mapbox.search.QueryType;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PlacesRepository {
    private static PlacesRepository instance;
    private static final Object sInstanceLock = new Object();

    private AppExecutors mAppExecutors;
    private PantryDB pantryDB;
    private PlaceDao dao;

    private PlacesDataSource dataSource;
    private SearchOptions searchOptions;

    private PlacesRepository(Application application) {
        pantryDB = PantryDB.getInstance(application);
        dao = pantryDB.getPlaceDao();
        mAppExecutors = AppExecutors.getInstance();
        dataSource = PlacesDataSource.getInstance(application);
        searchOptions = new SearchOptions.Builder(new SearchOptions())
                .types(QueryType.POI)
                .fuzzyMatch(true)
                .limit(10)
                .build();
    }

    public static PlacesRepository getInstance(Application application) {
        PlacesRepository i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new PlacesRepository(application);
                    i = instance;
                }
            }
        }
        return i;
    }

    public LiveData<Resource<List<? extends PlaceSearchSuggestion>>> search(String query) {

        final MediatorLiveData<List<? extends PlaceSearchSuggestion>> dbAdapter = new MediatorLiveData<>();

        String finalQuery = query != null ? query.trim() : "";
        String dbQuery = finalQuery.length() > 0
                ? "%" + finalQuery.trim().toLowerCase(Locale.ROOT) + "%"
                : "";

        LiveData<List<Place>> dbSource = dbQuery.length() > 0
                ? dao.search("%" + dbQuery + "%")
                : dao.getAll();

        dbAdapter.addSource(dbSource, places -> {
            if( places != null && !places.isEmpty() ) {
                mAppExecutors.diskIO().execute(() -> {
                    dbAdapter.postValue(places.stream()
                            .map(Converters::fromPlace)
                            .collect(Collectors.toList()));
                });
            }
            else {
                dbAdapter.setValue(Collections.emptyList());
            }
        });

        return new NetworkBoundResource<List<? extends PlaceSearchSuggestion>, List<? extends SearchSuggestion>>(mAppExecutors) {

            @Override
            protected void saveCallResult(List<? extends SearchSuggestion> items) {
                mAppExecutors.mainThread().execute(() -> {
                    dbAdapter.removeSource(dbSource);
                });

                dbAdapter.postValue(items.stream()
                        .map(Converters::fromSuggestion)
                        .collect(Collectors.toList()));
            }

            @Override
            protected boolean shouldFetch(@Nullable List<? extends PlaceSearchSuggestion> data) {
                return true;
            }

            @Override
            protected LiveData<List<? extends PlaceSearchSuggestion>> loadFromDb() {
                return dbAdapter;
            }

            @Override
            protected LiveData<ApiResponse<List<? extends SearchSuggestion>>> createCall() {
                return dataSource.search(finalQuery, searchOptions);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Place>> get(PlaceSearchSuggestion preference) {

        boolean isAPIPreference = preference instanceof PlaceSearchSuggestionBuilder.APISearchSuggestion;

        LiveData<ApiResponse<SearchResult>> apiCall = isAPIPreference
                ? dataSource.getPlace(((PlaceSearchSuggestionBuilder.APISearchSuggestion) preference).getResult())
                : null;
        LiveData<Place> dbSource = dao.getPlace(preference.getId());

        return new NetworkBoundResource<Place, SearchResult>(mAppExecutors) {

            @Override
            protected void saveCallResult(SearchResult item) {
                dao.insertPlace(Converters.fromResult(item));
            }

            @Override
            protected boolean shouldFetch(@Nullable Place data) {
                return isAPIPreference;
            }

            @Override
            protected LiveData<Place> loadFromDb() {
                return dbSource;
            }

            @Override
            protected LiveData<ApiResponse<SearchResult>> createCall() {
                return apiCall;
            }
        }.asLiveData();
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
