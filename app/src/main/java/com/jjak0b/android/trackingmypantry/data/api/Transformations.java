package com.jjak0b.android.trackingmypantry.data.api;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

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

    /**
     * Behaves similar like {@link Transformations#switchMap(LiveData, Function) }
     * so when source resource is not succeeded, forward any states to final result, otherwise forward the state of the switchMapFunction
     * @param mSource
     * @param switchMapFunction
     * @param <I>
     * @param <O>
     * @return a LiveData mapped from source to type <Y> by delegating to the LiveData returned
     * by applying switchMapFunction to each value set. value has an error, forward the error to final result
     */
    @MainThread
    @NonNull
    public static <I, O> LiveData<Resource<O>> forward(
            @NonNull LiveData<Resource<I>> mSource,
            @NonNull final androidx.arch.core.util.Function<Resource<I>, LiveData<Resource<O>>> switchMapFunction) {
        final MediatorLiveData<Resource<O>> mediator = new MediatorLiveData<>();
        mediator.addSource(mSource, resource -> {
            // simulate an API behaviour
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (mSource != null) {
                        mediator.removeSource(mSource);
                    }
                    // forward function result
                    LiveData<Resource<O>> switchedLD = androidx.lifecycle.Transformations
                            .switchMap(mSource, switchMapFunction);
                    mediator.addSource(
                            switchedLD,
                            mediator::setValue
                    );
                    break;
                case ERROR:
                    if (mSource != null) {
                        mediator.removeSource(mSource);
                    }
                    // forward new value as error response
                    mediator.setValue(Resource.error(resource.getError(), null));
                    break;
                default:
                    // we are waiting
                    mediator.setValue(Resource.loading(null));
                    break;
            }
        });

        return mediator;
    }

    /**
     * Behaves similar like {@link #forward(LiveData, Function) } but once
     * so when source resource is not succeeded, forward any states to final result, otherwise forward the state of the switchMapFunction
     * @param mSource
     * @param switchMapFunction
     * @param <I>
     * @param <O>
     * @return a LiveData mapped from source to type <Y> by delegating to the LiveData returned
     * by applying switchMapFunction to each value set. value has an error, forward the error to final result
     */
    @MainThread
    @NonNull
    public static <I, O> LiveData<Resource<O>> forwardOnce(
            @NonNull LiveData<Resource<I>> mSource,
            @NonNull final androidx.arch.core.util.Function<Resource<I>, LiveData<Resource<O>>> switchMapFunction) {
        final MediatorLiveData<Resource<O>> mediator = new MediatorLiveData<>();
        mediator.addSource(mSource, resource -> {
            // simulate an API behaviour
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (mSource != null) {
                        mediator.removeSource(mSource);
                    }
                    // forward function result
                    LiveData<Resource<O>> switchedLD = androidx.lifecycle.Transformations
                            .switchMap(mSource, switchMapFunction);
                    mediator.addSource(
                            switchedLD,
                            oResource -> {
                                mediator.setValue(oResource);
                                if( oResource.getStatus() != Status.LOADING ) {
                                    mediator.removeSource(switchedLD);
                                }
                            });

                    break;
                case ERROR:
                    if (mSource != null) {
                        mediator.removeSource(mSource);
                    }
                    // forward new value as error response
                    mediator.setValue(Resource.error(resource.getError(), null));
                    break;
                default:
                    // we are waiting
                    mediator.setValue(Resource.loading(null));
                    break;
            }
        });

        return mediator;
    }

    /**
     * Call the syncGetter into the asyncExecutor's thread
     * if it throws any error, they will be forwarded to the returned resource
     * otherwise set the value on the result as success
     * @param asyncExecutor
     * @param mainExecutor
     * @param syncGetter
     * @param <I>
     * @param <O>
     * @return the live data simulating an async api behavior
     */
    public static <I, O> LiveData<Resource<O>> simulateApi(
            @NonNull final Executor asyncExecutor,
            @NonNull final Executor mainExecutor,
            @NonNull final Callable<O> syncGetter
    ) {
        final MediatorLiveData<Resource<O>> apiResponse = new MediatorLiveData<>();
        apiResponse.setValue(Resource.loading(null));

        asyncExecutor.execute(() -> {
            try {
                O result = syncGetter.call();
                mainExecutor.execute(() -> {
                    apiResponse.setValue(Resource.success(result));
                });
            }
            // forward any error to caller
            catch (Throwable error) {
                mainExecutor.execute(() -> {
                    apiResponse.setValue(Resource.error(error, null));
                });
            }
        });

        return apiResponse;
    }


    public static <T> boolean onValid(Resource<T> resource, Callback<T> onValidCallBack) {
        if( resource == null ) return  false;

        if( resource.getStatus() == Status.SUCCESS ) {
            if( onValidCallBack != null ) {
                onValidCallBack.apply(resource.getData());
            }
            return true;
        }
        else {
            return false;
        }
    }

    public interface Callback<T> {
        void apply(T value);
    }
}
