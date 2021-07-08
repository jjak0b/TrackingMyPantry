package com.jjak0b.android.trackingmypantry.data.services.remote;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.model.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.model.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.model.Vote;

import java.util.List;

import retrofit2.Call;
import retrofit2.adapter.guava.GuavaCallAdapterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RemoteProductsAPIService {
    /**
     * @see GuavaCallAdapterFactory provided exception and results in callback
     * @param authorization
     * @param barcode
     * @return
     */
    @GET("products")
    ListenableFuture<ProductsList> getProducts(
            @Header("Authorization") String authorization,
            @Query("barcode") String barcode
    );

    /**
     * @see GuavaCallAdapterFactory provided exception and results in callback
     * @param authorization
     * @param product
     * @return
     */
    @POST("products")
    ListenableFuture<CreateProduct> postProduct(
            @Header("Authorization") String authorization,
            @Body CreateProduct product
    );

    /**
     * @see GuavaCallAdapterFactory provided exception and results in callback
     * @param authorization
     * @param productId
     * @return
     */
    @DELETE("products/{id}")
    ListenableFuture<Void> removeProduct(
            @Header("Authorization") String authorization,
            @Path("id") String productId
    );

    /**
     * @see GuavaCallAdapterFactory provided exception and results in callback
     * @param authorization
     * @param vote
     * @return
     */
    @POST("votes")
    ListenableFuture<Void> voteProduct(
            @Header("Authorization") String authorization,
            @Body Vote vote
    );

}
