package com.jjak0b.android.trackingmypantry.data.services;

import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.services.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.services.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.services.API.Vote;
import com.jjak0b.android.trackingmypantry.data.services.API.VoteResponse;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RemoteProductsAPIService {

    @GET("products")
    LiveData<ApiResponse<ProductsList>> getProducts(
            @Header("Authorization") String authorization,
            @Query("barcode") String barcode
    );

    @POST("products")
    LiveData<ApiResponse<CreateProduct>> postProduct(
            @Header("Authorization") String authorization,
            @Body CreateProduct product
    );


    @DELETE("products/{id}")
    LiveData<ApiResponse<UserProduct>> removeProduct(
            @Header("Authorization") String authorization,
            @Path("id") String productId
    );

    @POST("votes")
    LiveData<ApiResponse<VoteResponse>> voteProduct(
            @Header("Authorization") String authorization,
            @Body Vote vote
    );

}
