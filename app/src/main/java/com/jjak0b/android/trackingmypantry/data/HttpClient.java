package com.jjak0b.android.trackingmypantry.data;

import com.google.common.util.concurrent.FutureCallback;
import com.google.gson.GsonBuilder;
import com.jjak0b.android.trackingmypantry.data.services.remote.RemoteProductsAPIService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.guava.GuavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpClient {
    private static Retrofit client;
    private static final String BASE_URL = "https://lam21.iot-prism-lab.cs.unibo.it/";

    public static Retrofit getInstance() {
        if (client == null) {

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();

            client = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(GuavaCallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create( gsonBuilder.create() )
                    )
                    .build();
        }
        return client;
    }
}
