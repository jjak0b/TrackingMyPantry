package com.jjak0b.android.trackingmypantry.data.api;

import androidx.annotation.NonNull;

import retrofit2.HttpException;

public class ApiHttpErrorResponse<T> extends ApiErrorResponse<T> {
    public ApiHttpErrorResponse(Throwable error) {
        super(error);
    }

    @NonNull
    public static <T> ApiResponse<T> create(@NonNull HttpException error) {
        switch (error.code()) {
            // Unauthorized
            case 401:
                return ApiResponse.create(new NotLoggedInException());
            default:
                return ApiResponse.create(new RemoteException());
        }
    }
}
