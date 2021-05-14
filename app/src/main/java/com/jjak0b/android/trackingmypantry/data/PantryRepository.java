package com.jjak0b.android.trackingmypantry.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.data.dataSource.LoginDataSource;
import com.jjak0b.android.trackingmypantry.data.dataSource.PantryDataSource;
import com.jjak0b.android.trackingmypantry.data.model.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.Vote;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PantryRepository {

    private static PantryRepository instance;

    private PantryDataSource remoteDataSource;
    private LoginRepository loginRepository;
    private MutableLiveData<List<Product>> matchingProductList;
    private MutableLiveData<String> requestToken;

    PantryRepository() {
        remoteDataSource = new PantryDataSource( LoginRepository.getInstance() );
    }

    public static PantryRepository getInstance() {
        if( instance == null ) {
            instance = new PantryRepository();
        }
        return instance;
    }

    public LiveData<List<Product>> getProducts(String barcode) {
        remoteDataSource.getProducts(barcode, new Callback<ProductsList>() {
            @Override
            public void onResponse(Call<ProductsList> call, Response<ProductsList> response) {
                if( response.isSuccessful() ) {
                    matchingProductList.setValue( response.body().getProducts() );
                    requestToken.setValue( response.body().getToken() );
                }
                else {

                }
            }

            @Override
            public void onFailure(Call<ProductsList> call, Throwable t) {

            }
        });

        return matchingProductList;
    }

    public void voteProduct( Vote vote ) {
        remoteDataSource.voteProduct(vote, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
}
