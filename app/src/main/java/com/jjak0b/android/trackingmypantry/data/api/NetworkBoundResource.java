package com.jjak0b.android.trackingmypantry.data.api;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;

/**
 * A generic class that can provide a resource backed by both the sqlite database and the network.
 *
 *
 * You can read more about it in the [Architecture
 * Guide](https://developer.android.com/arch).
 * @param <ResultType>
 * @param <RequestType>
</RequestType></ResultType> */
public abstract class NetworkBoundResource<ResultType, RequestType> {

    private AppExecutors appExecutors;
    private MediatorLiveData<Resource<ResultType>> result;

    @MainThread
    public NetworkBoundResource(@NonNull AppExecutors _appExecutors) {
        appExecutors = _appExecutors;
        result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));

        LiveData<ResultType> dbSource = loadFromDb();

        result.addSource(dbSource, data -> {
            result.removeSource(dbSource);

            if (shouldFetch(data)) {
                fetchFromNetwork(dbSource);
            }
            else {
                result.addSource(dbSource, newData -> {
                    setValue(Resource.success(newData));
                });
            }
        });
    }

    @MainThread
    private void setValue(Resource<ResultType> newValue) {
        if (result.getValue() != newValue) {
            result.setValue(newValue);
        }
    }

    private void fetchFromNetwork (LiveData<ResultType> dbSource){
        LiveData<ApiResponse<RequestType>> apiResponse = createCall();
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(dbSource, newData -> {
            setValue(Resource.loading(newData));
        });

        result.addSource(apiResponse, response -> {
            result.removeSource(apiResponse);
            result.removeSource(dbSource);
            if( response instanceof ApiSuccessResponse) {
                appExecutors.diskIO().execute(() -> {
                    saveCallResult(processResponse((ApiSuccessResponse<RequestType>) response));
                    appExecutors.mainThread().execute(() -> {
                        // we specially request a new live data,
                        // otherwise we will get immediately last cached value,
                        // which may not be updated with latest results received from network.
                        result.addSource(loadFromDb(),newData -> {
                            setValue(Resource.success(newData));
                        });
                    });
                });
            }
            else if( response instanceof ApiEmptyResponse){
                appExecutors.mainThread().execute(() -> {
                    // reload from disk whatever we had
                    result.addSource(loadFromDb(), newData -> {
                        setValue(Resource.success(newData));
                    });
                });
            }
            else if( response instanceof ApiErrorResponse){
                Throwable cause = ((ApiErrorResponse<RequestType>) response).getError();
                appExecutors.diskIO().execute(() -> {
                    onFetchFailed(cause);
                    appExecutors.mainThread().execute(() -> {
                        result.addSource(dbSource, newData -> {
                            setValue(Resource.error(cause, newData));
                        });
                    });
                });
            }
        });
    }

    @WorkerThread
    protected void onFetchFailed(Throwable cause) {}

    public LiveData<Resource<ResultType>> asLiveData() {
        return result;
    }

    @WorkerThread
    protected RequestType processResponse(@NonNull ApiSuccessResponse<RequestType> response){
        return response.getBody();
    }

    @WorkerThread
    protected abstract void saveCallResult ( RequestType item);

    @MainThread
    protected abstract boolean shouldFetch ( @Nullable ResultType data );

    // Single source of truth
    @MainThread
    protected abstract LiveData<ResultType> loadFromDb ();

    @MainThread
    protected abstract LiveData<ApiResponse<RequestType>> createCall ();
}