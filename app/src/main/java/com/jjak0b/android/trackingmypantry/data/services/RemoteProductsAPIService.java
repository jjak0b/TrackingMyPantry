package com.jjak0b.android.trackingmypantry.data.services;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.services.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.services.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.services.API.Vote;
import com.jjak0b.android.trackingmypantry.data.services.API.VoteResponse;

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

    @GET("products")
    LiveData<ApiResponse<ProductsList>> _getProducts(
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

    @POST("products")
    LiveData<ApiResponse<CreateProduct>> _postProduct(
            @Header("Authorization") String authorization,
            @Body CreateProduct product
    );


    @DELETE("products/{id}")
    LiveData<ApiResponse<Product>> removeProduct(
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

    @POST("votes")
    LiveData<ApiResponse<VoteResponse>> _voteProduct(
            @Header("Authorization") String authorization,
            @Body Vote vote
    );

}
