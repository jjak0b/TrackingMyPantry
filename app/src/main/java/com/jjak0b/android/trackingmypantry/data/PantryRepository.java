package com.jjak0b.android.trackingmypantry.data;

import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.GsonBuilder;
import com.jjak0b.android.trackingmypantry.data.dataSource.PantryDataSource;
import com.jjak0b.android.trackingmypantry.data.model.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.model.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.Vote;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.services.local.PantryDB;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PantryRepository {

    private static PantryRepository instance;
    private static final String TAG = "PantryRepo";

    private PantryDataSource remoteDataSource;
    private MediatorLiveData<List<Product>> matchingProductList;
    private LiveData<List<Product>> matchingProductListLocal;
    private MutableLiveData<String> requestToken;
    private PantryDB pantryDB;
    private static final int nTHREADS = 2;
    private static final ListeningExecutorService executor =
            MoreExecutors.listeningDecorator( Executors.newFixedThreadPool(nTHREADS) );
    private static Executor mainExecutor;
    PantryRepository(final Context context) {
        remoteDataSource = new PantryDataSource( LoginRepository.getInstance() );
        matchingProductList = new MediatorLiveData<>();
        requestToken = new MutableLiveData<>();
        pantryDB = PantryDB.getInstance( context );
        if( mainExecutor == null )
            mainExecutor = ContextCompat.getMainExecutor( context );
    }

    public static PantryRepository getInstance( Context context ) {
        if( instance == null ) {
            instance = new PantryRepository( context );
        }
        return instance;
    }

    private ListeningExecutorService getExecutor(){
        return executor;
    }

    public void updateMatchingProducts( String barcode ) {
        if( barcode == null ) {
            requestToken.postValue( null );
            Log.e(TAG, "reset request token" );
            matchingProductList.postValue( new ArrayList<>(0) );
            Log.e(TAG, "reset product list" );
            return;
        }
        Log.e(TAG, "request product list by " + barcode );
        Futures.addCallback(
                remoteDataSource.getProducts(barcode),
                new FutureCallback<ProductsList>() {
                    @Override
                    public void onSuccess(@NullableDecl ProductsList result) {
                        try {
                            Log.d(TAG, "updateMatchingProducts: " + new GsonBuilder()
                                    .setPrettyPrinting().create().toJson( result ) );
                        }
                        catch ( Exception e ) {
                            Log.w(TAG, "updateMatchingProducts raw error" + e );
                        }

                        requestToken.postValue( result.getToken() );
                        matchingProductList.postValue( result.getProducts() );
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e(TAG, "updateMatchingProducts " +  t );

                        // provide local products
                        updateMatchingProductsUsingLocal( barcode );

                    }
                },
                mainExecutor
        );
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

    public ListenableFuture voteProduct( String productId, int rating ) {
        Vote vote = new Vote(requestToken.getValue(), productId, rating );
        ListenableFuture future = remoteDataSource.voteProduct( vote );
        Futures.addCallback(
                future,
                new FutureCallback<Object>() {
                    @Override
                    public void onSuccess(@NullableDecl Object result) {
                        Log.d( TAG, "Voted " + vote );
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e( TAG, "Failed to vote " + vote, t );
                    }
                },
                MoreExecutors.directExecutor()
        );
        return future;
    }

    public LiveData<List<ProductWithTags>> getProductsWithTags( /*TODO: pass Filter*/){
        return pantryDB.getProductDao().getAllProductsWithTags();
    }

    public LiveData<ProductWithTags> getProductWithTags(String productId ) {
        return pantryDB.getProductDao()
                .getProductWithTags( productId );
    }

    /**
     * Blocking call
     * @param p
     * @param tags
     * @return
     */
    private void addProductLocal( Product p, List<ProductTag> tags ){
        pantryDB.getProductDao().insertProductAndAssignedTags( p, tags );
    }

    /**
     * Will add the product on local and remote DB.
     * if product's id is null will be considered as a new product and will post it on remote
     * @param p
     * @param tags
     * with cause
     *
     */
    public ListenableFuture<Product> addProduct(final Product p, List<ProductTag> tags ) {
        // TODO: pass ProductBundle to add Product details to remote and and product instances details to local

        Log.d( TAG, "addProduct: " + p );

        boolean fetchProductFromResponse = p.getId() == null;
        ListenableFuture beforeLocal;

        if( !fetchProductFromResponse ){
            beforeLocal = Futures.immediateFuture( p );
        }
        else {
            beforeLocal = Futures.transform(
                    remoteDataSource.postProduct(new CreateProduct(p, requestToken.getValue())),
                    new Function<CreateProduct, Product>() {
                        @NullableDecl
                        @Override
                        public Product apply(@NullableDecl CreateProduct input) {
                            Product p = new Product.Builder()
                                    .from(input)
                                    .build();
                            Log.d( TAG, "addProduct - fetched product from remote: " + p );
                            return p;
                        }
                    },
                    getExecutor()
            );
        }

        ListenableFuture<Product> afterLocal = Futures.transform(
                beforeLocal,
                new Function<Product, Product>() {
                    @Override
                    public Product apply(@NullableDecl Product input) {
                        addProductLocal(input, tags);;
                        Log.d( TAG, "addProduct - added product to local" + input );
                        return input;
                    }
                },
                pantryDB.getDBWriteExecutor()
        );

        return afterLocal;
    };


    public ListenableFuture<Long> addProductInstanceGroup(ProductInstanceGroup instanceGroup, Product product, Pantry pantry ) {
        instanceGroup.setPantryId( pantry.getId() );
        instanceGroup.setProductId( product.getId() );
        return Futures.submit(
                new Callable<Long>() {
                    @Override
                    public Long call() {
                        return pantryDB.getProductInstanceDao().insertAll( instanceGroup )[ 0 ];
                    }
                },
                pantryDB.getDBWriteExecutor()
        );
    }

    public ListenableFuture<Pantry> addPantry( Pantry pantry ) {
        Log.d( TAG, "adding pantry to local " + pantry );

        return Futures.transform(
                pantryDB.getPantryDao().addPantry( pantry ),
                new Function<Long, Pantry>() {
                    @Override
                    public Pantry apply(@NullableDecl Long pantryId) {
                        if( pantryId >= 0 ){
                            pantry.setId( pantryId );
                        }

                        Log.d( TAG, "added pantry to local " + pantry );
                        return pantry;
                    }
                },
                pantryDB.getDBWriteExecutor()
        );
    }

    public LiveData<List<Pantry>> getPantries(){
        return pantryDB.getPantryDao().getAll();
    }

    public LiveData<List<PantryWithProductInstanceGroups>> getPantriesWithProductInstanceGroupsOf(String productID ){
        return pantryDB.getPantryDao().getAllThatContains(productID);
    }
}
