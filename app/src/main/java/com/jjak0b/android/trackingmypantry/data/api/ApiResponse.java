package com.jjak0b.android.trackingmypantry.data.api;

import android.text.TextUtils;

import java.io.IOException;
import retrofit2.Response;

/**
 * Common class used by API responses.
 * @param <T> the type of the response object
</T> */
public abstract class ApiResponse<T> {
    public static <T> ApiErrorResponse<T> create(Throwable error) {
        return new ApiErrorResponse(error.getMessage() != null ? error.getMessage() : "unknown error");
    }

    public static <T> ApiResponse<T> create(Response<T> response) {
        if (response.isSuccessful()) {
            T body = response.body();
            if (body == null || response.code() == 204) { // no content
                return new ApiEmptyResponse();
            }
            else {
                return new ApiSuccessResponse(body);
            }
        }
        else {
            String msg = null;
            try {
                msg = response.errorBody() != null ? response.errorBody().string() : null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            String errorMsg = TextUtils.isEmpty(msg) ? response.message() : msg;
            return new ApiErrorResponse(errorMsg != null ? errorMsg : "unknown error");
        }
    }
}

/**
 * separate class for HTTP 204 resposes so that we can make ApiSuccessResponse's body non-null.
 */
public class ApiEmptyResponse<T> extends ApiResponse<T> {}

public class ApiErrorResponse<T> extends ApiResponse<T> {
    private String errorMessage;

    public ApiErrorResponse(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

public class ApiSuccessResponse<T> extends ApiResponse<T> {
    private T body;

    ApiSuccessResponse( T body ) {
        super();
        this.body = body;
    }

    public T getBody() {
        return body;
    }
}
