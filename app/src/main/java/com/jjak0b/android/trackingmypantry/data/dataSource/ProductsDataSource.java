package com.jjak0b.android.trackingmypantry.data.dataSource;

import android.content.Context;

import com.jjak0b.android.trackingmypantry.data.HttpClient;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.repositories.AuthRepository;
import com.jjak0b.android.trackingmypantry.data.services.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.services.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.services.API.Vote;
import com.jjak0b.android.trackingmypantry.data.services.API.VoteResponse;
import com.jjak0b.android.trackingmypantry.data.services.RemoteProductsAPIService;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class ProductsDataSource {

    private static ProductsDataSource instance;
    private static final Object sInstanceLock = new Object();

    private RemoteProductsAPIService service;
    private AuthRepository mAuthRepository;

    private ProductsDataSource( final Context context) {
        service = HttpClient.getInstance()
                .create(RemoteProductsAPIService.class);
        mAuthRepository = AuthRepository.getInstance(context);
    }

    public static ProductsDataSource getInstance(Context context) {
        ProductsDataSource i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new ProductsDataSource(context);
                    i = instance;
                }
            }
        }
        return i;
    }

    public LiveData<ApiResponse<ProductsList>> search(@NonNull String barcode ) {
        return Transformations.switchMap(mAuthRepository.requireAuthorization(), authorization -> {
            return service._getProducts(authorization.getData(), barcode);
        });
    }

    public LiveData<ApiResponse<VoteResponse>> postPreference(@NonNull Vote vote ) {
        return Transformations.switchMap(mAuthRepository.requireAuthorization(), authorization -> {
            return service._voteProduct(authorization.getData(), vote);
        });
    }

    public LiveData<ApiResponse<CreateProduct>> postProduct(@NonNull CreateProduct product ) {
        return Transformations.switchMap(mAuthRepository.requireAuthorization(), authorization -> {
            return service._postProduct(authorization.getData(), product);
        });
    }

    public LiveData<ApiResponse<Void>> delete(@NonNull String productID) {
        return Transformations.switchMap(mAuthRepository.requireAuthorization(), authorization -> {
            return service.removeProduct(authorization.getData(), productID);
        });
    }
}
