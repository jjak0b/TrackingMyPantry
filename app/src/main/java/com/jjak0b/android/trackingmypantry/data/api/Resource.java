package com.jjak0b.android.trackingmypantry.data.api;

public class Resource<T> {
    Status status;
    T data;
    String message;
    Throwable error;

    public Resource(Status status, T data, Throwable error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public Resource( T data, Throwable error) {
        this.status = Status.ERROR;
        this.data = data;
        this.error = error;
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data,null);
    }

    public static <T> Resource<T> error(Throwable error, T data) {
        return new Resource<>(data, error);
    }

    public static <T> Resource<T> loading(T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public Throwable getError() {
        return error;
    }
}
