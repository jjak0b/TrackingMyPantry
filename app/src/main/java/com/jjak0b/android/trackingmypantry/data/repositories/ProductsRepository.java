package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.NetworkBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.RemoteException;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.dataSource.ProductsDataSource;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.ProductDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.services.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.services.API.ProductsList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductsRepository {
    private static final String TAG = "ProductsRepository";

    private static ProductsRepository instance;
    private static final Object sInstanceLock = new Object();

    private ProductsDataSource dataSource;
    private AuthRepository authRepository;
    private AppExecutors mAppExecutors;
    private PantryDB pantryDB;
    private ProductDao productDao;

    private MediatorLiveData<Resource<String>> mSearchToken;
    private LiveData<Resource<String>> mSearchTokenSource;
    private LiveData<Resource<String>> mDefaultTokenSource;

    ProductsRepository(final Context context) {
        pantryDB = PantryDB.getInstance( context );
        productDao = pantryDB.getProductDao();
        dataSource = ProductsDataSource.getInstance(context);
        authRepository = AuthRepository.getInstance(context);
        mAppExecutors = AppExecutors.getInstance();
        mSearchToken = new MediatorLiveData<>();
        mDefaultTokenSource = new MutableLiveData<>(getCleanToken());
        mSearchTokenSource = mDefaultTokenSource;
        mSearchToken.addSource(mSearchTokenSource, resource -> {
            mSearchToken.setValue(getCleanToken());
        });
    }

    public static ProductsRepository getInstance(Context context) {
        ProductsRepository i = instance;
        if( i == null ) {
            synchronized (sInstanceLock) {
                i = instance;
                if (i == null) {
                    instance = new ProductsRepository(context);
                    i = instance;
                }
            }
        }
        return i;
    }

    private Resource<String> getCleanToken() {
        return Resource.error(new IllegalStateException("No search token acquired"), null);
    }

    private LiveData<Resource<List<Product>>> search(String barcode) {
        Log.d(TAG, "Request product list by " + barcode );

        final MediatorLiveData<List<Product>> mSearchResult = new MediatorLiveData<>();
        mSearchResult.setValue(new ArrayList<>(0));
        LiveData<ApiResponse<ProductsList>> mApiResponse = dataSource.search(barcode);

        final MutableLiveData<String> mSearchString = new MutableLiveData<>(null);
        LiveData<Resource<String>> mTokenSource = new NetworkBoundResource<String, ProductsList>(mAppExecutors) {
             @Override
             protected void saveCallResult(ProductsList item) {
                 mSearchString.postValue(item.getToken());
             }

             @Override
             protected boolean shouldFetch(@Nullable String data) {
                 return true;
             }

            @Override
            protected void onFetchFailed(Throwable cause) {
                 unsetSearchToken();
            }

            @Override
             protected LiveData<String> loadFromDb() {
                 return mSearchString;
             }

             @Override
             protected LiveData<ApiResponse<ProductsList>> createCall() {
                 return mApiResponse;
             }
        }.asLiveData();
        setSearchToken(mTokenSource);

        LiveData<Resource<List<Product>>> mListResponse = new NetworkBoundResource<List<Product>, ProductsList>(mAppExecutors) {
            @Override
            protected void saveCallResult(ProductsList item) {
                mSearchResult.postValue(item.getProducts());
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Product> data) {
                return true;
            }

            @Override
            protected void onFetchFailed(Throwable cause) {
                LiveData<List<Product>> mDBSource = productDao.getProductsByBarcode(barcode);
                mSearchResult.addSource(mDBSource, mSearchResult::setValue);
            }

            @Override
            protected LiveData<List<Product>> loadFromDb() {
                return mSearchResult;
            }

            @Override
            protected LiveData<ApiResponse<ProductsList>> createCall() {
                return mApiResponse;
            }

        }.asLiveData();

        return mListResponse;
    }

    @MainThread
    private void unsetSearchToken() {
        if( mSearchTokenSource != null )
            mSearchToken.removeSource(mSearchTokenSource);
        mSearchTokenSource = mDefaultTokenSource;
        mSearchToken.addSource(mSearchTokenSource, resource -> mSearchToken.setValue(resource));
    }

    private void setSearchToken( LiveData<Resource<String>> mSource ) {
        // detach old token
        if( mSearchTokenSource != null )
            mSearchToken.removeSource(mSearchTokenSource);

        if( mSource != null ){
            // attach new token
            mSearchTokenSource = mSource;
            mSearchToken.addSource(mSearchTokenSource, resource -> {
                mSearchToken.setValue(resource);
            });
        }
        else {
            unsetSearchToken();
        }
    }
    public LiveData<Resource<ProductWithTags>> add(@NonNull ProductWithTags data ) {

        final MediatorLiveData<Resource<ProductWithTags>> mResult = new MediatorLiveData<>();

        LiveData<Resource<ProductWithTags>> mProductResultSource = new NetworkBoundResource<ProductWithTags, CreateProduct>(mAppExecutors) {
            @Override
            protected void saveCallResult(CreateProduct item) {
                // consume token
                mAppExecutors.mainThread().execute(() -> unsetSearchToken());

                ProductWithTags newPWtags = new ProductWithTags();
                newPWtags.product = new Product.Builder().from(item).build();;
                newPWtags.tags = new ArrayList<>(data.tags);

                productDao.insertProductAndAssignedTags(newPWtags.product, newPWtags.tags);
            }

            @Override
            protected void onFetchFailed(Throwable cause) {
                if( cause instanceof RemoteException ) {
                    Log.e(TAG, "Unable to add product to remote " + data.product, cause);
                }
                else if( cause instanceof IOException ){
                    // if we are offline or due to I/O errors add it locally
                    mAppExecutors.diskIO().execute(() -> {
                        productDao.insertProductAndAssignedTags(data.product, data.tags);
                    });
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable ProductWithTags data) {
                // fetch only if we have no id, and let generate it to remote
                return data == null || data.product.getId() == null;
            }

            @Override
            protected LiveData<ProductWithTags> loadFromDb() {
                return productDao.getProductWithTags(data.product.getId());
            }

            @Override
            protected LiveData<ApiResponse<CreateProduct>> createCall() {
                return Transformations.switchMap(mSearchToken, tokenRes -> {
                    return dataSource.postProduct(new CreateProduct(
                            data.product,
                            tokenRes.getData()
                    ));
                });
            }
        }.asLiveData();

        return mResult;
    }

    public LiveData<Resource<ProductWithTags>> update(@NonNull ProductWithTags data ) {
        return new NetworkBoundResource<ProductWithTags, ProductWithTags>(mAppExecutors) {
            @Override
            protected void saveCallResult(ProductWithTags item) {
                productDao.updateProductAndAssignedTags(item.product, item.tags);
            }

            @Override
            protected boolean shouldFetch(@Nullable ProductWithTags data) {
                return true;
            }

            @Override
            protected LiveData<ProductWithTags> loadFromDb() {
                return productDao.getProductWithTags(data.product.getId());
            }

            @Override
            protected LiveData<ApiResponse<ProductWithTags>> createCall() {
                return Transformations.adapt(new MutableLiveData<>(Resource.success(data)));
            }
        }.asLiveData();
    }

    public LiveData<Resource<Void>> remove(@NonNull Product product) {
        final MutableLiveData<Void> mResult = new MutableLiveData<>(null);
        return new NetworkBoundResource<Void, Void>(mAppExecutors) {
            @Override
            protected void saveCallResult(Void item) {
                productDao.remove(product);
                mResult.postValue(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable Void data) {
                return true;
            }

            @Override
            protected LiveData<Void> loadFromDb() {
                return mResult;
            }

            @Override
            protected LiveData<ApiResponse<Void>> createCall() {
                return dataSource.delete(product.getId());
            }
        }.asLiveData();
    }
}
