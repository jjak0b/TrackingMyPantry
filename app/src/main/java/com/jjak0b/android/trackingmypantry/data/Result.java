package com.jjak0b.android.trackingmypantry.data;

/**
 * A generic class that holds a result success w/ data or an error exception.
 */
public class Result<S, E> {
    // hide the private constructor to limit subclass types (Success, Error)
    private Result() {
    }

    @Override
    public String toString() {
        if (this instanceof Result.Success) {
            Result.Success success = (Result.Success) this;
            return "Success[data=" + success.getData().toString() + "]";
        } else if (this instanceof Result.Error) {
            Result.Error error = (Result.Error) this;
            return "Error[exception=" + error.getError().toString() + "]";
        }
        return "";
    }

    // Success sub-class
    public final static class Success<S, E> extends Result<S, E> {
        private S data;

        public Success(S data) {
            this.data = data;
        }

        public S getData() {
            return this.data;
        }
    }

    // Error sub-class
    public final static class Error<S, E> extends Result<S, E> {
        private E error;

        public Error(E error) {
            this.error = error;
        }

        public E getError() {
            return this.error;
        }
    }
}