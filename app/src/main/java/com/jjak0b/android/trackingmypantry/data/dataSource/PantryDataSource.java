package com.jjak0b.android.trackingmypantry.data.dataSource;

import android.content.Context;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.jjak0b.android.trackingmypantry.data.HttpClient;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.repositories.AuthRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.LoginRepository;
import com.jjak0b.android.trackingmypantry.data.services.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.services.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.services.API.Vote;
import com.jjak0b.android.trackingmypantry.data.services.API.VoteResponse;
import com.jjak0b.android.trackingmypantry.data.services.RemoteProductsAPIService;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.concurrent.ExecutionException;

import retrofit2.adapter.guava.GuavaCallAdapterFactory;

public class PantryDataSource {

    private static PantryDataSource instance;
    private RemoteProductsAPIService service;
    private LoginRepository authRepository;
    private AuthRepository mAuthRepository;

    public PantryDataSource( LoginRepository repository ) {
        service = HttpClient.getInstance()
                .create(RemoteProductsAPIService.class);
        this.authRepository = repository;
    }


    public static PantryDataSource getInstance(LoginRepository repository) {
        if( instance == null ) {
            instance = new PantryDataSource(repository);
        }

        return instance;
    }

    public static PantryDataSource getInstance(final Context context) {
        if( instance == null ) {
            instance = new PantryDataSource(LoginRepository.getInstance(context));
            instance.mAuthRepository = AuthRepository.getInstance(context);
        }

        return instance;
    }

    /**
     * @param barcode
     * @implNote see
     * <ul>
     *     <li> {@linkplain LoginRepository#requireAuthorization(boolean)} for exception </li>
     *     <li> {@linkplain GuavaCallAdapterFactory} for exceptions</li>
     * </ul>
     * @return
     */
    public ListenableFuture<ProductsList> getProducts(@NonNull String barcode ) {
        return Futures.transformAsync(
                authRepository.requireAuthorization(false),
                new AsyncFunction<String, ProductsList>() {
                    @NullableDecl
                    @Override
                    public ListenableFuture<ProductsList> apply(@NullableDecl String authorization) {
                        return service.getProducts(
                                authorization,
                                barcode
                        );
                    }
                },
                MoreExecutors.directExecutor()
        );
    }

    public LiveData<ApiResponse<ProductsList>> _getProducts(@NonNull String barcode ) {
        return Transformations.switchMap(mAuthRepository.requireAuthorization(), authorization -> {
            return service._getProducts(authorization.getData(), barcode);
        });
    }

    public LiveData<ApiResponse<VoteResponse>> _voteProduct(@NonNull Vote vote ) {
        return Transformations.switchMap(mAuthRepository.requireAuthorization(), authorization -> {
            return service._voteProduct(authorization.getData(), vote);
        });
    }
    /**
     * @implNote see
     * <ul>
     *     <li> {@linkplain LoginRepository#requireAuthorization(boolean)} for exception </li>
     *     <li> {@linkplain GuavaCallAdapterFactory} for exceptions</li>
     * </ul>
     * @param vote
     * @return
     */
    public ListenableFuture voteProduct(@NonNull Vote vote ) {
        return Futures.transformAsync(
                authRepository.requireAuthorization(false),
                new AsyncFunction<String, Void>() {
                    @NullableDecl
                    @Override
                    public ListenableFuture<Void> apply(@NullableDecl String authorization) {
                        return service.voteProduct(
                                authorization,
                                vote
                        );
                    }
                },
                MoreExecutors.directExecutor()
        );
    }

    /**
     * @implNote see
     * <ul>
     *     <li> {@linkplain LoginRepository#requireAuthorization(boolean)} for exception </li>
     *     <li> {@linkplain GuavaCallAdapterFactory} for exceptions</li>
     * </ul>
     * @param product
     * @return
     */
    public ListenableFuture<CreateProduct> postProduct(@NonNull CreateProduct product ) {

        return Futures.transformAsync(
                authRepository.requireAuthorization(false),
                new AsyncFunction<String, CreateProduct>() {
                    @NullableDecl
                    @Override
                    public ListenableFuture<CreateProduct> apply(@NullableDecl String authorization) {
                        return service.postProduct(
                                authorization,
                                product
                        );
                    }
                },
                MoreExecutors.directExecutor()
        );
    }
}
