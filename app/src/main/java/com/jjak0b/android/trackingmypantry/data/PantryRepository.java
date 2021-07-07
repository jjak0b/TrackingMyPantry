package com.jjak0b.android.trackingmypantry.data;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.google.gson.GsonBuilder;
import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.auth.AuthException;
import com.jjak0b.android.trackingmypantry.data.dataSource.LoginDataSource;
import com.jjak0b.android.trackingmypantry.data.dataSource.PantryDataSource;
import com.jjak0b.android.trackingmypantry.data.model.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.model.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.Vote;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.services.local.PantryDB;

import java.util.ArrayList;
import java.util.List;

import java9.util.concurrent.CompletableFuture;
import java9.util.function.Supplier;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PantryRepository {

    private static PantryRepository instance;
    private static final String TAG = "PantryRepo";

    private PantryDataSource remoteDataSource;
    private MediatorLiveData<List<Product>> matchingProductList;
    private LiveData<List<Product>> matchingProductListLocal;
    private MutableLiveData<String> requestToken;
    private PantryDB pantryDB;


    PantryRepository(final Context context) {
        remoteDataSource = new PantryDataSource( LoginRepository.getInstance() );
        matchingProductList = new MediatorLiveData<>();
        requestToken = new MutableLiveData<>();
        pantryDB = PantryDB.getInstance( context );
    }

    public static PantryRepository getInstance( Context context ) {
        if( instance == null ) {
            instance = new PantryRepository( context );
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

                    // provide local products
                    updateMatchingProductsUsingLocal( barcode );
                }
            }

            @Override
            public void onFailure(Call<ProductsList> call, Throwable t) {
                Log.e(TAG, "Unable to fetch " + t );

                // provide local products
                updateMatchingProductsUsingLocal( barcode );
            }
        });
    }

    private void updateMatchingProductsUsingLocal( String barcode ) {
        if( matchingProductListLocal != null ) {
            matchingProductList.removeSource(matchingProductListLocal);
        }
        matchingProductListLocal = pantryDB.getProductDao()
                .getProductsByBarcode(barcode);
        matchingProductList.addSource(
                matchingProductListLocal,
                products -> {
                    Log.d( TAG, "update matching products from local: " + products );
                    matchingProductList.postValue( products );
                }
        );
    }

    public LiveData<List<Product>> getMatchingProducts() {
        return matchingProductList;
    }

    public LiveData<List<ProductTag>> getAllProductTags() {
        return pantryDB.getProductDao().getAllTags();
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

    public LiveData<List<Product>> getProducts( /*TODO: pass Filter*/){
        return pantryDB.getProductDao().getAll();
    }

    public LiveData<ProductWithTags> getProductWithTags(String productId ) {
        return pantryDB.getProductDao()
                .getProductWithTags( productId );
    }

    private void addProductLocal( Product p, List<ProductTag> tags ){
        pantryDB.getProductDao().insertProductAndAssignedTags( p, tags );
    }

    /**
     * Will add the product on local and remote DB.
     * if product's id is null will be considered as a new product and will post it on remote
     * @param p
     * @param tags
     * @return a {@link CompletableFuture} that on Exceptionally will provide a {@link Throwable}
     * with cause
     * an {@link IllegalStateException} if a server error happens
     *
     */
    public CompletableFuture<Void> addProduct(final Product p, List<ProductTag> tags ) {
        // TODO: pass ProductBundle to add Product details to remote and and product instances details to local

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        Log.d( TAG, "posting product" + p );

        boolean fetchProductFromResponse = p.getId() == null;

        if( !fetchProductFromResponse ){
            Log.d( TAG, "posting product to local" + p );
            try{
                PantryDB.getDBWriteExecutor().execute(() -> {
                    try {
                        addProductLocal(p, tags);
                        completableFuture.complete(null);
                    }
                    catch (Exception e){
                        completableFuture.completeExceptionally(e);
                    }
                });
            }
            catch ( Exception e ){
                completableFuture.completeExceptionally(e);
            }
        }
        else {
            remoteDataSource.postProduct(new CreateProduct(p, requestToken.getValue()), new Callback<CreateProduct>() {
                @Override
                public void onResponse(Call<CreateProduct> call, Response<CreateProduct> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "post product complete " + response.toString() + " " + new GsonBuilder().setPrettyPrinting().create().toJson(response.body()));

                        // fetch data with generated from remote
                        Product fetchedP = new Product.Builder()
                                .from(p)
                                .build();

                        Log.d(TAG, "posting product to local after fetch " + p);
                        try {
                            PantryDB.getDBWriteExecutor().execute(() -> {
                                try {
                                    addProductLocal(fetchedP, tags);
                                    completableFuture.complete(null);
                                }
                                catch (Exception e){
                                    completableFuture.completeExceptionally(e);
                                }
                            });
                        }
                        catch (Exception e) {
                            completableFuture.completeExceptionally(e);
                        }
                    }
                    else {
                        Log.e(TAG, "post product error" + response.toString());
                        completableFuture.completeExceptionally(new IllegalStateException(response.toString()));
                    }
                }

                @Override
                public void onFailure(Call<CreateProduct> call, Throwable t) {
                    Log.e(TAG, "Unable to post product " + t);
                    // the error may be network related or auth related
                    completableFuture.completeExceptionally(t);
                }
            });
        }

        return completableFuture;
    }
}
