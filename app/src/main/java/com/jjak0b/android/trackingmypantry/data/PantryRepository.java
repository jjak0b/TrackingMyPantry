package com.jjak0b.android.trackingmypantry.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

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
import com.jjak0b.android.trackingmypantry.data.model.Place;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.model.Vote;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductInstanceGroupInfo;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PurchaseInfoWithPlace;
import com.jjak0b.android.trackingmypantry.data.services.local.PantryDB;
import com.jjak0b.android.trackingmypantry.data.services.local.ProductDao;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PantryRepository {

    private static PantryRepository instance;
    private static final Object sInstanceLock = new Object();

    private static final String TAG = "PantryRepo";

    private PantryDataSource remoteDataSource;
    private LoginRepository authRepository;
    private ExpirationEventsRepository expirationEventsRepository;

    private MediatorLiveData<List<Product>> matchingProductList;
    private LiveData<List<Product>> matchingProductListLocal;
    private MutableLiveData<String> requestToken;
    private PantryDB pantryDB;
    private static final int nTHREADS = 2;
    private static final ListeningExecutorService executor =
            MoreExecutors.listeningDecorator( Executors.newFixedThreadPool(nTHREADS) );
    private static Executor mainExecutor;

    PantryRepository(final Context context) {
        authRepository = LoginRepository.getInstance(context);
        expirationEventsRepository = ExpirationEventsRepository.getInstance(context);
        remoteDataSource = PantryDataSource.getInstance(context);
        matchingProductList = new MediatorLiveData<>();
        requestToken = new MutableLiveData<>();
        pantryDB = PantryDB.getInstance( context );
        if( mainExecutor == null )
            mainExecutor = ContextCompat.getMainExecutor( context );
    }

    public static PantryRepository getInstance(Context context) {
        PantryRepository i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new PantryRepository(context);
                    i = instance;
                }
            }
        }
        return i;
    }

    private ListeningExecutorService getExecutor(){
        return executor;
    }

    public ListenableFuture<ProductsList> updateMatchingProducts(String barcode ) {
        matchingProductList.postValue( null );
        Log.e(TAG, "reset product list" );

        if( barcode == null ) {
            requestToken.postValue( null );
            Log.e(TAG, "reset request token" );
            return null;
        }
        Log.e(TAG, "request product list by " + barcode );
        ListenableFuture<ProductsList> futureList = remoteDataSource.getProducts(barcode);
        Futures.addCallback(
                futureList,
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
        return futureList;
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
                        expirationEventsRepository.updateExpiration(input.getId(), null, null);
                        Log.d( TAG, "sync " + input );
                        return input;
                    }
                },
                pantryDB.getDBWriteExecutor()
        );

        return afterLocal;
    };


    public ListenableFuture<Void> updateProductLocal(@NonNull ProductWithTags productWithTags ) {

        Log.d(TAG, "updateProduct: " + productWithTags);
        final ProductDao dao = pantryDB.getProductDao();

        ListenableFuture<Void> afterRemove = Futures.transformAsync(
                dao.getProductAssignedTags(productWithTags.product.getId()),
                input -> {
                    Log.d(TAG, "removing tags :" + input);
                    return dao.removeAssignedTags(input);
                },
                pantryDB.getDBWriteExecutor()
        );

        return Futures.transform(
                afterRemove,
                input -> {
                    Log.d(TAG, "inserting tags :" + productWithTags.tags);
                    dao.updateProductAndAssignedTags( productWithTags.product, productWithTags.tags);
                    return null;
                },
                pantryDB.getDBWriteExecutor()
        );
    }

    public ListenableFuture<Long> addProductInstanceGroup(ProductInstanceGroup instanceGroup, Product product, Pantry pantry ) {
        instanceGroup.setPantryId( pantry.getId() );
        instanceGroup.setProductId( product.getId() );

        return Futures.transform(
                pantryDB.getProductInstanceDao().insertAll( instanceGroup ),
                input -> {
                    expirationEventsRepository.insertExpiration(input[0]);
                    return input[0];
                },
                getExecutor()
        );
    }

    public ListenableFuture<Void> deleteProductInstanceGroup(ProductInstanceGroup... entry) {
       ListenableFuture<Void> future = pantryDB.getProductInstanceDao().deleteAll(entry);
       Futures.addCallback(
               future,
               new FutureCallback<Void>() {
                   @Override
                   public void onSuccess(@NullableDecl Void result) {
                       for (ProductInstanceGroup group : entry) {
                           expirationEventsRepository.removeExpiration(group.getId());
                       }
                   }

                   @Override
                   public void onFailure(Throwable t) { }
               },
               getExecutor()
       );
       return future;
    }

    public ListenableFuture<Void> updatedProductInstanceGroup( ProductInstanceGroup... entry ){
        ListenableFuture<Void> future = pantryDB.getProductInstanceDao().updateAll(entry);
        Futures.addCallback(
                future,
                new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(@NullableDecl Void result) {
                        for (ProductInstanceGroup group : entry) {
                            expirationEventsRepository.updateExpiration(null, null, group.getId());
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) { }
                },
                getExecutor()
        );
        return future;
    }

    public ListenableFuture<Void> moveProductInstanceGroupToPantry( ProductInstanceGroup entry, Pantry pantry, int quantity ){

        if( quantity >= entry.getQuantity() ){
            entry.setPantryId(pantry.getId());
            ListenableFuture<Void> future = pantryDB.getProductInstanceDao().updateAll(entry);

            Futures.addCallback(
                    future,
                    new FutureCallback<Void>() {
                        @Override
                        public void onSuccess(@NullableDecl Void result) {
                            expirationEventsRepository.updateExpiration(null, null, entry.getId() );
                        }

                        @Override
                        public void onFailure(Throwable t) {}
                    },
                    getExecutor()
            );
            return future;
        }
        else {
            ProductInstanceGroup newGroup = ProductInstanceGroup.from(entry);
            newGroup.setId(0);
            newGroup.setPantryId(pantry.getId());
            newGroup.setQuantity(quantity);
            entry.setQuantity(entry.getQuantity() - quantity);

            ListenableFuture<List<Object>> futureList = Futures.allAsList(
                    pantryDB.getProductInstanceDao().updateAll(entry),
                    pantryDB.getProductInstanceDao().insertAll(newGroup)
            );

            Futures.addCallback(
                    futureList,
                    new FutureCallback<List<Object>>() {
                        @Override
                        public void onSuccess(@NullableDecl List<Object> results) {
                            expirationEventsRepository.updateExpiration(null, null, entry.getId() );
                            expirationEventsRepository.insertExpiration(((long[]) results.get(1))[0]);
                        }

                        @Override
                        public void onFailure(Throwable t) { }
                    },
                    getExecutor()
            );

            return Futures.transform(
                    futureList,
                    input -> null,
                    getExecutor()
            );
        }
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
                        Log.d( TAG, "added pantry to local " + pantry + " with id " + pantryId );

                        expirationEventsRepository.updateExpiration(null, pantry.getId(), null );

                        return pantry;
                    }
                },
                MoreExecutors.directExecutor()
        );
    }

    public LiveData<List<Pantry>> getPantries(){
        return pantryDB.getPantryDao().getAll();
    }

    public LiveData<List<PantryWithProductInstanceGroups>> getPantriesWithProductInstanceGroupsOf(String productID ){

        return Transformations.switchMap(
                pantryDB.getPantryDao().getAllThatContains(productID),
                pantries -> {
                    final MutableLiveData<List<PantryWithProductInstanceGroups>> mData = new MutableLiveData<>();

                    getExecutor().submit( () -> {
                        ArrayList<PantryWithProductInstanceGroups> dataList = new ArrayList<>(pantries.size());

                        for (Pantry pantry : pantries) {
                            PantryWithProductInstanceGroups item = new PantryWithProductInstanceGroups();
                            item.pantry = pantry;
                            item.instances =  pantryDB.getProductInstanceDao().getAllInstancesOfProduct(productID, pantry.getId());
                            dataList.add(item);
                        }
                        mData.postValue(dataList);
                    });

                    return mData;
                }
        );
    }

    public List<ProductInstanceGroupInfo> getListInfoOfAll(@Nullable String productID, @Nullable Long pantryID){
        return pantryDB.getProductInstanceDao().getListInfoOfAll(productID, pantryID);
    }
    public LiveData<List<ProductInstanceGroupInfo>> getLiveInfoOfAll(@Nullable String productID, @Nullable Long pantryID){
        return pantryDB.getProductInstanceDao().getLiveInfoOfAll(productID, pantryID);
    }
    public ListenableFuture<List<ProductInstanceGroupInfo>> getInfoOfAll(@Nullable String productID, @Nullable Long pantryID){
        return pantryDB.getProductInstanceDao().getInfoOfAll(productID, pantryID);
    }
    public ListenableFuture<List<ProductInstanceGroupInfo>> getInfoOfAll(long... groupID) {
        return pantryDB.getProductInstanceDao().getInfoOfAll(groupID);
    }

    public LiveData<Place> getPlace(String placeId) {
        return pantryDB.getPlaceDao().getPlace(placeId);
    }

    public ListenableFuture<Place> addPlace(Place place) {
        return Futures.transform(
                pantryDB.getPlaceDao().insertPlace(place),
                input -> place,
                MoreExecutors.directExecutor()
        );
    }

    public ListenableFuture<Long> addPurchaseInfo(PurchaseInfo purchaseInfo) {
        return pantryDB.getPurchaseInfoDao().insertPurchaseInfo(purchaseInfo);
    }

    public LiveData<List<PurchaseInfoWithPlace>> getAllPurchaseInfo(@NonNull String productID) {
        return pantryDB.getPurchaseInfoDao().getAllPurchaseInfo(productID);
    }
}
