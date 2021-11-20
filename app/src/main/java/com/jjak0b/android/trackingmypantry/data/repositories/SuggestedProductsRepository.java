package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.NetworkBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.dataSource.ProductsDataSource;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.services.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.services.API.Vote;
import com.jjak0b.android.trackingmypantry.data.services.API.VoteResponse;

import java.util.ArrayList;
import java.util.List;

public class SuggestedProductsRepository {

    private static final String TAG = "SuggestedProductsRepository";

    private static SuggestedProductsRepository instance;
    private static final Object sInstanceLock = new Object();

    private AppExecutors mAppExecutors;
    private ProductsDataSource productsDataSource;
    private PantryDB mPantryDB;
    private MediatorLiveData<Resource<List<Product>>> mProducts;

    private MutableLiveData<Resource<String>> mRequestToken;
    private LiveData<Resource<List<Product>>> mProductsResource;
    private MediatorLiveData<List<Product>> mProductsSource;

    public SuggestedProductsRepository(final Context context) {
        this.mAppExecutors = AppExecutors.getInstance();
        this.productsDataSource = ProductsDataSource.getInstance(context);
        this.mPantryDB = PantryDB.getInstance(context);

        this.mProducts = new MediatorLiveData<>();
        this.mProductsResource = new MutableLiveData<>(Resource.loading(null));
        this.mProductsSource = new MediatorLiveData<>();
    }

    public static SuggestedProductsRepository getInstance(Context context) {
        SuggestedProductsRepository i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new SuggestedProductsRepository(context);
                    i = instance;
                }
            }
        }
        return i;
    }

    private LiveData<Resource<String>> getRequestToken() {
        return mRequestToken;
    }

    public LiveData<Resource<List<Product>>> getProducts(String barcode) {

        LiveData<Resource<List<Product>>> mSource;
        // attach a new resource
        if( barcode != null ) {
            mSource = getProductsBy(barcode);
        }
        else {
            mSource = new MutableLiveData<>(Resource.loading(null));
        }

        mProducts.addSource(mSource, resource -> {
            mProducts.setValue(resource);
        });

        // detach current account resource
        if( mProductsResource != null ) {
            mProducts.removeSource(mProductsResource);
        }
        mProductsResource = mSource;

        return mProducts;
    }

    private LiveData<Resource<List<Product>>> getProductsBy(String barcode) {
        Log.d(TAG, "Request product list by " + barcode );
        final MediatorLiveData<List<Product>> mResult = new MediatorLiveData<>();
        mResult.setValue(new ArrayList<>(0));

        return new NetworkBoundResource<List<Product>, ProductsList>(mAppExecutors) {

            @Override
            protected void saveCallResult(ProductsList item) {
                mResult.postValue(item.getProducts());
                mRequestToken.postValue(Resource.success(item.getToken()));
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Product> data) {
                return true;
            }

            @Override
            protected void onFetchFailed(Throwable cause) {
                LiveData<List<Product>> mDBSource = mPantryDB.getProductDao()
                        .getProductsByBarcode(barcode);
                mResult.addSource(
                        mDBSource,
                        mResult::setValue
                );
                mRequestToken.setValue(Resource.error(cause, null));
            }

            @Override
            protected LiveData<List<Product>> loadFromDb() {
                return mResult;
            }

            @Override
            protected LiveData<ApiResponse<ProductsList>> createCall() {
                return productsDataSource.search(barcode);
            }

        }.asLiveData();
    }

    public LiveData<Resource<VoteResponse>> vote(@NonNull String productId, int rating ) {
        final MutableLiveData<VoteResponse> mResult = new MutableLiveData<>(null);
        return new NetworkBoundResource<VoteResponse, VoteResponse>(mAppExecutors) {

            @Override
            protected void saveCallResult(VoteResponse item) {
                mResult.postValue(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable VoteResponse data) {
                return true;
            }

            @Override
            protected void onFetchFailed(Throwable cause) {
                Log.e( TAG, "Failed to vote ", cause);
            }

            @Override
            protected LiveData<VoteResponse> loadFromDb() {
                return mResult;
            }

            @Override
            protected LiveData<ApiResponse<VoteResponse>> createCall() {
                return Transformations.switchMap(getRequestToken(), resourceRequestToken -> {
                    return productsDataSource.postPreference(new Vote(
                            resourceRequestToken.getData(),
                            productId,
                            rating
                    ));
                });
            }
        }.asLiveData();
    }
}
