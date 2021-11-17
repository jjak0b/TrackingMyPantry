package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.NetworkBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.dataSource.PantryDataSource;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.ProductDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.services.API.CreateProduct;

import java.util.List;

public class ProductsRepository {
    private static ProductsRepository instance;
    private static final Object sInstanceLock = new Object();

    private PantryDataSource remoteDataSource;
    private AuthRepository authRepository;
    private AppExecutors mAppExecutors;
    private PantryDB pantryDB;
    private ProductDao productDao;

    ProductsRepository(final Context context) {
        pantryDB = PantryDB.getInstance( context );
        productDao = pantryDB.getProductDao();
        remoteDataSource = PantryDataSource.getInstance(context);
        authRepository = AuthRepository.getInstance(context);
        mAppExecutors = AppExecutors.getInstance();
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


    public LiveData<Resource<ProductWithTags>> add(@NonNull ProductWithTags data, @NonNull String requestToken ) {

        final MutableLiveData<Product> mFetchedProduct = new MutableLiveData<>(data.product);

        return new NetworkBoundResource<ProductWithTags, CreateProduct>(mAppExecutors) {
            @Override
            protected void saveCallResult(CreateProduct item) {
                Product p =  new Product.Builder().from(item).build();
                productDao.insertProductAndAssignedTags(p, data.tags);
                mFetchedProduct.postValue(p);
            }

            @Override
            protected boolean shouldFetch(@Nullable ProductWithTags data) {
                // fetch only if we have no id, and let generate it to remote
                return data == null || data.product.getId() == null;
            }

            @Override
            protected LiveData<ProductWithTags> loadFromDb() {
                return androidx.lifecycle.Transformations.switchMap(mFetchedProduct, input -> {
                    if( input != null && input.getId() != null)
                        return productDao.getProductWithTags(input.getId());
                    else
                        return new MutableLiveData<>(null);
                });
            }

            @Override
            protected LiveData<ApiResponse<CreateProduct>> createCall() {
                return remoteDataSource._postProduct(new CreateProduct(
                        data.product,
                        requestToken
                ));
            }
        }.asLiveData();
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
                return remoteDataSource.deleteProduct(product.getId());
            }
        }.asLiveData();
    }
}
