package com.jjak0b.android.trackingmypantry.data.dataSource;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.maps.ResourceOptionsManager;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.SearchSelectionCallback;
import com.mapbox.search.SearchSuggestionsCallback;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Response;

public class PlacesDataSource {
    private static PlacesDataSource instance;
    private static final Object sInstanceLock = new Object();

    private SearchEngine service;

    public PlacesDataSource(Application application) {
        String accessToken = ResourceOptionsManager.Companion.getDefault(application, null)
                .getResourceOptions().getAccessToken();

        MapboxSearchSdk.initialize(
                application,
                accessToken,
                LocationEngineProvider.getBestLocationEngine(application)
        );
        service = MapboxSearchSdk.getSearchEngine();
    }

    public static PlacesDataSource getInstance(Application application) {
        PlacesDataSource i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new PlacesDataSource(application);
                    i = instance;
                }
            }
        }
        return i;
    }

    public LiveData<ApiResponse<List<? extends SearchSuggestion>>> search(String query, SearchOptions searchOptions) {
        // create and API adapter
        return new LiveData<ApiResponse<List<? extends SearchSuggestion>>>() {
            private AtomicBoolean started = new AtomicBoolean(false);
            final SearchSuggestionsCallback callback = new SearchSuggestionsCallback() {
                @Override
                public void onSuggestions(@NonNull List<? extends SearchSuggestion> list, @NonNull ResponseInfo responseInfo) {
                    postValue(ApiResponse.create(Response.success(list)));
                }

                @Override
                public void onError(@NonNull Exception e) {
                    postValue(ApiResponse.create(e));
                }
            };
            protected void onActive() {
                super.onActive();
                if (started.compareAndSet(false, true)) {
                    service.search(query, searchOptions, callback);
                }
            }
        };
    }

    public LiveData<ApiResponse<SearchResult>> getPlace(SearchSuggestion preference) {
        return new LiveData<ApiResponse<SearchResult>>() {
            private AtomicBoolean started = new AtomicBoolean(false);
            final SearchSelectionCallback callback = new SearchSelectionCallback() {
                @Override
                public void onResult(@NonNull SearchSuggestion searchSuggestion, @NonNull SearchResult searchResult, @NonNull ResponseInfo responseInfo) {
                    postValue(ApiResponse.create(Response.success(searchResult)));
                }

                @Override
                public void onError(@NonNull Exception e) {
                    postValue(ApiResponse.create(e));
                }

                // unused
                @Override
                public void onCategoryResult(@NonNull SearchSuggestion searchSuggestion, @NonNull List<? extends SearchResult> list, @NonNull ResponseInfo responseInfo) {}
                @Override
                public void onSuggestions(@NonNull List<? extends SearchSuggestion> list, @NonNull ResponseInfo responseInfo) {}
            };
            protected void onActive() {
                super.onActive();
                if (started.compareAndSet(false, true)) {
                    service.select(preference, callback);
                }
            }
        };
    }
}
