package com.jjak0b.android.trackingmypantry.data;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.GsonBuilder;
import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.dataSource.LoginDataSource;
import com.jjak0b.android.trackingmypantry.data.dataSource.PantryDataSource;
import com.jjak0b.android.trackingmypantry.data.model.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.model.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.Vote;

import java.util.ArrayList;
import java.util.List;

import java9.util.concurrent.CompletableFuture;
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
            requestToken.postValue( null );
            matchingProductList.postValue( new ArrayList<>(0) );
            Log.e(TAG, "null barcode" );
            return;
        }
        Log.e(TAG, "not null barcode" );
        remoteDataSource.getProducts(barcode, new Callback<ProductsList>() {
            @Override
            public void onResponse(Call<ProductsList> call, Response<ProductsList> response) {
                if( response.isSuccessful() ) {
                    try {
                        Log.d(TAG, "Fetch products raw " + response.raw().body().string() );

                    }
                    catch ( Exception e ) {
                        Log.e(TAG, "Fetch products raw e" + e );
                    }

                    Log.d(TAG, "Fetch products complete " +  response.toString() + " " + new GsonBuilder().setPrettyPrinting().create().toJson( response.body() ) );
                    requestToken.postValue( response.body().getToken() );
                    matchingProductList.postValue( response.body().getProducts() );
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

    public CompletableFuture<Void> addProduct( Product p ) {
        // TODO: pass ProductBundle to add Product details to remote and and product instances details to local

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        Log.d( TAG, "posting product" + p );
        remoteDataSource.postProduct(new CreateProduct( p, requestToken.getValue() ), new Callback<CreateProduct>() {
            @Override
            public void onResponse(Call<CreateProduct> call, Response<CreateProduct> response) {
                if( response.isSuccessful() ) {
                    Log.d(TAG, "post product complete " +  response.toString() + " " + new GsonBuilder().setPrettyPrinting().create().toJson( response.body() ) );
                    completableFuture.complete(null);
                }
                else {
                    Log.e(TAG, "post product error" +  response.toString() );
                    completableFuture.completeExceptionally( new IllegalStateException(response.toString()) );
                }
            }

            @Override
            public void onFailure(Call<CreateProduct> call, Throwable t) {
                Log.e(TAG, "Unable to post product " + t );
                completableFuture.completeExceptionally(t);
            }
        });

        return completableFuture;
    }
}
