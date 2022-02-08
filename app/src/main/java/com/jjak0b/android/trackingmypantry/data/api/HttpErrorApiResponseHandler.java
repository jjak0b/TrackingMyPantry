package com.jjak0b.android.trackingmypantry.data.api;

import java.util.function.Function;

import retrofit2.HttpException;

public class HttpErrorApiResponseHandler {

    /**
     * Wrapper to handle a {@ApiResponse} if an error of {@RemoteException} occurred
     * and caused by any {@HttpExpection} with the provided error code
     * @param response
     * @param code
     * @param function
     * @return
     */
    public static <T> boolean handle(ApiResponse<T> response, final int code, Function<HttpException, Boolean> function) {
        if( function == null ) return false;

        if( response instanceof ApiErrorResponse) {
            Throwable error = ((ApiErrorResponse<T>) response).getError();
            if( error instanceof RemoteException){
                RemoteException remoteError = (RemoteException) error;
                if( remoteError.getCause() instanceof HttpException ) {
                    HttpException httpError = (HttpException) remoteError.getCause();
                    if( httpError.code() == code ) {
                        return function.apply(httpError);
                    }
                }
            }
        }
        return false;
    }
}
