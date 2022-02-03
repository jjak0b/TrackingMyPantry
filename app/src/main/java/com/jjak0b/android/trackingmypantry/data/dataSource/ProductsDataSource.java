package com.jjak0b.android.trackingmypantry.data.dataSource;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jjak0b.android.trackingmypantry.data.HttpClient;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.repositories.AuthRepository;
import com.jjak0b.android.trackingmypantry.data.services.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.services.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.services.API.Vote;
import com.jjak0b.android.trackingmypantry.data.services.API.VoteResponse;
import com.jjak0b.android.trackingmypantry.data.services.RemoteProductsAPIService;

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

    /**
     * Fetch a products list available on remote by barcode, with a request token that should be used
     * to {@link #postProduct(CreateProduct)} or to {@link #postPreference(Vote)}.
     * If response contains errors, then should be:
     * <ul>
     *     <li>{@link AuthRepository#requireAuthorization()}'s exceptions</li>
     *     <li>{@link com.jjak0b.android.trackingmypantry.data.api.RemoteException}</li>
     *     <li>{@link java.io.IOException}</li>
     * </ul>
     * @@implNote It requires a valid user to logged on @{@link AuthRepository}
     * @see AuthRepository#requireAuthorization()
     * @param barcode
     * @return
     */
    public LiveData<ApiResponse<ProductsList>> search(@NonNull String barcode ) {
        return Transformations.switchMap(mAuthRepository.requireAuthorization(), authorization -> {
            Log.e("Products data source", "searching by " + barcode + " with " +authorization);
            return service.getProducts(authorization.getData(), barcode);
        });
    }

    /**
     * Add a product preference on remote, the @{@link Vote} should contains the request token got from {@link #search(String)}.
     * If response contains errors, then should be:
     * <ul>
     *     <li>{@link AuthRepository#requireAuthorization()}'s exceptions</li>
     *     <li>{@link com.jjak0b.android.trackingmypantry.data.api.RemoteException}</li>
     *     <li>{@link java.io.IOException}</li>
     * </ul>
     * @@implNote It requires a valid user to logged on @{@link AuthRepository}
     * @see AuthRepository#requireAuthorization()
     * @param vote
     * @return
     */
    public LiveData<ApiResponse<VoteResponse>> postPreference(@NonNull Vote vote ) {
        return Transformations.switchMap(mAuthRepository.requireAuthorization(), authorization -> {
            return service.voteProduct(authorization.getData(), vote);
        });
    }

    /**
     * Add a product entry on remote, the @{@link CreateProduct} should contains the request token got from {@link #search(String)}.
     * If response contains errors, then should be:
     * <ul>
     *     <li>{@link AuthRepository#requireAuthorization()}'s exceptions</li>
     *     <li>{@link com.jjak0b.android.trackingmypantry.data.api.RemoteException}</li>
     *     <li>{@link java.io.IOException}</li>
     * </ul>
     * @@implNote It requires a valid user to logged on @{@link AuthRepository}
     * @see AuthRepository#requireAuthorization()
     * @param product
     * @return
     */
    public LiveData<ApiResponse<CreateProduct>> postProduct(@NonNull CreateProduct product ) {
        return Transformations.switchMap(mAuthRepository.requireAuthorization(), authorization -> {
            return service.postProduct(authorization.getData(), product);
        });
    }

    /**
     * Delete a product entry on remote, the productID should be of a product got from {@link #search(String)}.
     * If response contains errors, then should be:
     * <ul>
     *     <li>{@link AuthRepository#requireAuthorization()}'s exceptions</li>
     *     <li>{@link com.jjak0b.android.trackingmypantry.data.api.RemoteException}</li>
     *     <li>{@link java.io.IOException}</li>
     * </ul>
     * @@implNote It requires a valid user to logged on @{@link AuthRepository}
     * @see AuthRepository#requireAuthorization()
     * @param productID
     * @return
     */
    public LiveData<ApiResponse<Product>> delete(@NonNull String productID) {
        return Transformations.switchMap(mAuthRepository.requireAuthorization(), authorization -> {
            return service.removeProduct(authorization.getData(), productID);
        });
    }
}
