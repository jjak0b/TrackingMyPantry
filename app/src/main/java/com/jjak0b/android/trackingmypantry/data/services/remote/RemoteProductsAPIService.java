package com.jjak0b.android.trackingmypantry.data.services.remote;

import com.google.gson.annotations.Expose;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.model.Vote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RemoteProductsAPIService {

    @GET("products")
    Call<List<Product>> getProducts(
            @Header("Authorization") String authorization,
            @Query("barcode") String barcode
    );

    @POST("products")
    Call<ProductsList> addProduct(
            @Header("Authorization") String authorization,
            @Body CreateProduct product
    );

    @DELETE("products/{id}")
    Call removeProduct(
            @Header("Authorization") String authorization,
            @Path("id") String productId
    );

    @POST("votes")
    Call voteProduct(
            @Header("Authorization") String authorization,
            @Body Vote vote
    );

    class ProductsList {
        @Expose
        String token;

        @Expose
        List<Product> products;

        public String getToken() { return token; }

        public List<Product> getProducts() { return products; }
    }

}
