package com.jjak0b.android.trackingmypantry.data.dataSource;

import com.jjak0b.android.trackingmypantry.data.HttpClient;
import com.jjak0b.android.trackingmypantry.data.LoginRepository;
import com.jjak0b.android.trackingmypantry.data.NotLoggedInException;
import com.jjak0b.android.trackingmypantry.data.model.API.AuthLoginResponse;
import com.jjak0b.android.trackingmypantry.data.model.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.model.LoginCredentials;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.RegisterCredentials;
import com.jjak0b.android.trackingmypantry.data.model.Vote;
import com.jjak0b.android.trackingmypantry.data.services.remote.RemoteAuthAPIService;
import com.jjak0b.android.trackingmypantry.data.services.remote.RemoteProductsAPIService;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
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

    public void getProducts(@NotNull String barcode, Callback<ProductsList> cb ) {
        if( authRepository.isLoggedIn() ) {
            service.getProducts(
                    "Bearer " + authRepository.getLoggedInUser().getValue().getAccessToken(),
                    barcode
            ).enqueue( cb );
        }
        else{
            cb.onFailure( null, new NotLoggedInException() );
        }
    }

    public void voteProduct( @NotNull Vote vote, Callback<Void> cb ) {
        if( authRepository.isLoggedIn() ) {
            service.voteProduct(
                    "Bearer " + authRepository.getLoggedInUser().getValue().getAccessToken(),
                    vote
            ).enqueue( cb );
        }
        else{
            cb.onFailure( null, new NotLoggedInException() );
        }

    }
}
