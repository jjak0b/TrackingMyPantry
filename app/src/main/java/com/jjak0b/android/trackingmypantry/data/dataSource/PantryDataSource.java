package com.jjak0b.android.trackingmypantry.data.dataSource;

import android.util.Log;

import com.jjak0b.android.trackingmypantry.data.HttpClient;
import com.jjak0b.android.trackingmypantry.data.LoginRepository;
import com.jjak0b.android.trackingmypantry.data.auth.AuthException;
import com.jjak0b.android.trackingmypantry.data.auth.AuthResultState;
import com.jjak0b.android.trackingmypantry.data.auth.NotLoggedInException;
import com.jjak0b.android.trackingmypantry.data.Result;
import com.jjak0b.android.trackingmypantry.data.model.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.model.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.model.Vote;
import com.jjak0b.android.trackingmypantry.data.services.remote.RemoteProductsAPIService;

import org.jetbrains.annotations.NotNull;

import java9.util.function.Consumer;
import retrofit2.Callback;

public class PantryDataSource {

    private static PantryDataSource instance;
    private RemoteProductsAPIService service;
    private LoginRepository authRepository;

    public PantryDataSource( LoginRepository repository ) {
        service = HttpClient.getInstance()
                .create(RemoteProductsAPIService.class);
        this.authRepository = repository;
    }

    public static PantryDataSource getInstance() {
        if( instance == null ) {
            instance = new PantryDataSource(LoginRepository.getInstance());
        }

        return instance;
    }

    /**
     *
     * @param barcode
     * @param cb callback to be called when error has been occurred. it will be:
     *           - onSuccess: will provide the result
     *           - onFailed: will provide:
     *              - a {@link AuthException} if an error happened during authentication
     *
     */
    public void getProducts(@NotNull String barcode, Callback<ProductsList> cb ) {

        authRepository.requireAuthorization(false)
                .thenAccept(new Consumer<Result<String, AuthResultState>>() {
                    @Override
                    public void accept(Result<String, AuthResultState> resultAuth ) {
                        if( resultAuth instanceof Result.Success ){
                            service.getProducts(
                                    ((Result.Success<String, AuthResultState>) resultAuth).getData(),
                                    barcode
                            ).enqueue( cb );
                        }
                        else {
                            Result.Error<String, AuthResultState> error = (Result.Error<String, AuthResultState>) resultAuth;
                            cb.onFailure( null, new AuthException( error.getError() ) );
                        }
                    }
                });
    }

    public void voteProduct( @NotNull Vote vote, Callback<Void> cb ) {
        authRepository.requireAuthorization(false)
                .thenAccept(new Consumer<Result<String, AuthResultState>>() {
                    @Override
                    public void accept(Result<String, AuthResultState> resultAuth ) {
                        if( resultAuth instanceof Result.Success ){
                            service.voteProduct(
                                    ((Result.Success<String, AuthResultState>) resultAuth).getData(),
                                    vote
                            ).enqueue( cb );
                        }
                        else {
                            Result.Error<String, AuthResultState> error = (Result.Error<String, AuthResultState>) resultAuth;
                            cb.onFailure( null, new AuthException( error.getError() ) );
                        }
                    }
                });
    }

    public void postProduct( @NotNull CreateProduct product, Callback<CreateProduct> cb ) {
        authRepository.requireAuthorization(false)
                .thenAccept(new Consumer<Result<String, AuthResultState>>() {
                    @Override
                    public void accept(Result<String, AuthResultState> resultAuth ) {
                        if( resultAuth instanceof Result.Success ){
                            service.postProduct(
                                    ((Result.Success<String, AuthResultState>) resultAuth).getData(),
                                    product
                            ).enqueue( cb );
                        }
                        else {
                            Result.Error<String, AuthResultState> error = (Result.Error<String, AuthResultState>) resultAuth;
                            cb.onFailure( null, new AuthException( error.getError() ) );
                        }
                    }
                });
    }
}
