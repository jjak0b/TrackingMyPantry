package com.jjak0b.android.trackingmypantry.data.api;

import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Common class used by API responses.
 * @param <T> the type of the response object
</T> */
public abstract class ApiResponse<T> {
    public static <T> ApiErrorResponse<T> create(Throwable error) {
        return new ApiErrorResponse<>(error);
    }

    public static <T> ApiResponse<T> create(Response<T> response) {
        if (response.isSuccessful()) {
            T body = response.body();
            if (body == null || response.code() == 204) { // no content
                return new ApiEmptyResponse<>();
            }
            else {
                return new ApiSuccessResponse<>(body);
            }
        }
        else {
            return new ApiErrorResponse<>(new RemoteException(new HttpException(response)));
        }
    }
}

/**
 * separate class for HTTP 204 resposes so that we can make ApiSuccessResponse's body non-null.
 */
class ApiEmptyResponse<T> extends ApiResponse<T> {}

class ApiErrorResponse<T> extends ApiResponse<T> {
    private Throwable error;

    public ApiErrorResponse(Throwable error) {
        super();
        this.error = error;
    }

    public String getErrorMessage() {
        return error != null && error.getMessage() != null ? error.getMessage() : "unknown error";
    }

    public Throwable getError() {
        return error;
    }
}

class ApiSuccessResponse<T> extends ApiResponse<T> {
    private T body;

    ApiSuccessResponse( T body ) {
        super();
        this.body = body;
    }

    public T getBody() {
        return body;
    }
}
