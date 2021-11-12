package com.jjak0b.android.trackingmypantry.data.api;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import retrofit2.Response;

public class Transformations {

    /**
     * Behaves similar like {@link androidx.lifecycle.Transformations#switchMap(LiveData, Function) }
     * but when source resource has an error, forward the error to final result instead of calling the switchMapFunction
     * @param mSource
     * @param switchMapFunction
     * @param <I>
     * @param <O>
     * @return a LiveData mapped from source to type <Y> by delegating to the LiveData returned
     * by applying switchMapFunction to each value set. value has an error, forward the error to final result
     */
    @MainThread
    @NonNull
    public static <I, O> LiveData<ApiResponse<O>> switchMap(
            @NonNull LiveData<Resource<I>> mSource,
            @NonNull final androidx.arch.core.util.Function<Resource<I>, LiveData<ApiResponse<O>>> switchMapFunction) {
        final MediatorLiveData<ApiResponse<O>> mediator = new MediatorLiveData<>();

        mediator.addSource(mSource, resource -> {
            // simulate an API behaviour
            switch (resource.getStatus()) {
                case SUCCESS:
                    LiveData<ApiResponse<O>> mResponse = switchMapFunction.apply(resource);
                    if (mSource != null) {
                        mediator.removeSource(mSource);
                    }

                    if(mResponse != null ) {
                        // provide new value on api response
                        mediator.addSource(mResponse, userApiResponse -> {
                            mediator.removeSource(mResponse);
                            mediator.setValue(userApiResponse);
                        });
                    }
                    break;
                case ERROR:
                    if (mSource != null) {
                        mediator.removeSource(mSource);
                    }
                    mediator.setValue(ApiResponse.create(resource.getError()));
                    break;
                default:
                    // do nothing
                    break;
            }
        });

        return mediator;
    }

    /**
     * Adapts a LiveData of a {@link Resource} to behave like a {@link ApiResponse}.
     * So when a resource is set as {@link Status#SUCCESS}, then the ApiResponse is a success response,
     * when a resource is set as {@link Status#ERROR}, then the ApiResponse is a failed response
     * but when a resource is set as {@link Status#LOADING} then won't be set a response
     * @param mSource
     * @param <T>
     * @return
     */
    @MainThread
    @NonNull
    public static <T> LiveData<ApiResponse<T>> adapt(@NonNull LiveData<Resource<T>> mSource) {
        final MediatorLiveData<ApiResponse<T>> mediator = new MediatorLiveData<>();

        mediator.addSource(mSource, resource -> {
            // simulate an API behaviour
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (mSource != null) {
                        mediator.removeSource(mSource);
                    }
                    // provide new value as api success response
                    mediator.setValue(ApiResponse.create(Response.success(resource.getData())));
                    break;
                case ERROR:
                    if (mSource != null) {
                        mediator.removeSource(mSource);
                    }
                    // provide new value as api error response
                    mediator.setValue(ApiResponse.create(resource.getError()));
                    break;
                default:
                    // do nothing, we are waiting
                    break;
            }
        });

        return mediator;
    }
}
