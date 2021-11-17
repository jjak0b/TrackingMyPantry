package com.jjak0b.android.trackingmypantry.data.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;

public abstract class IOBoundResource<ResultType> extends NetworkBoundResource<ResultType, ResultType> {

    public IOBoundResource(@NonNull AppExecutors _appExecutors) {
        super(_appExecutors);
    }

    protected void saveCallResult(ResultType item) {

    }

    protected boolean shouldFetch(@Nullable ResultType data) {
        return false;
    }

    protected LiveData<ApiResponse<ResultType>> createCall() {
        return null;
    }

    public static <ResultType> LiveData<Resource<ResultType>> adapt(
            @NonNull AppExecutors appExecutors,
            @NonNull LiveData<ResultType> mSource
    ) {
        return new IOBoundResource<ResultType>(appExecutors) {

            @Override
            protected LiveData<ResultType> loadFromDb() {
                return mSource;
            }
        }.asLiveData();
    }
}
