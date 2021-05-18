package com.jjak0b.android.trackingmypantry.data;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.GsonBuilder;
import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.dataSource.LoginDataSource;
import com.jjak0b.android.trackingmypantry.data.dataSource.PantryDataSource;
import com.jjak0b.android.trackingmypantry.data.model.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.Vote;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PantryRepository {

    private static PantryRepository instance;
    private static final String TAG = "PantryRepo";

    private PantryDataSource remoteDataSource;
    private MutableLiveData<List<Product>> matchingProductList;
    private MutableLiveData<String> requestToken;

    PantryRepository() {
        remoteDataSource = new PantryDataSource( LoginRepository.getInstance() );
        matchingProductList = new MutableLiveData<>();
        requestToken = new MutableLiveData<>();
    }

    public static PantryRepository getInstance() {
        if( instance == null ) {
            instance = new PantryRepository();
        }
        return instance;
    }

    public void updateMatchingProducts( String barcode ) {
        if( barcode == null ) {
            requestToken.setValue( null );
            matchingProductList.setValue( new ArrayList<>(0) );
            return;
        }

        remoteDataSource.getProducts(barcode, new Callback<ProductsList>() {
            @Override
            public void onResponse(Call<ProductsList> call, Response<ProductsList> response) {
                if( response.isSuccessful() ) {
                    requestToken.setValue( response.body().getToken() );
                    matchingProductList.setValue( response.body().getProducts() );
                    Log.d(TAG, "Fetch products complete " +  response.toString() + " " + new GsonBuilder().setPrettyPrinting().create().toJson( response.body() ) );
                }
                else {
                    Log.e(TAG, "Fetch products error" +  response.toString() );
                }
            }

            @Override
            public void onFailure(Call<ProductsList> call, Throwable t) {
                Log.e(TAG, "Unable to fetch " + t );
            }
        });
    }

    public LiveData<List<Product>> getMatchingProducts() {
        return matchingProductList;
    }

    public void voteProduct( String productId, int rating ) {
        remoteDataSource.voteProduct(new Vote(requestToken.getValue(), productId, rating ), new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if( response.isSuccessful() ) {
                    Log.d(TAG, "vote product '" + productId + "' complete " +  response.toString() );
                }
                else {
                    Log.e(TAG, "failed to vote product '" + productId + "'" + response.toString() );
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Unable to vote '" + productId + "'" + t );
            }
        });
    }
}
