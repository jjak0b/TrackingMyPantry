package com.jjak0b.android.trackingmypantry.data.repositories;

import android.content.Context;
import android.util.Log;
import android.webkit.URLUtil;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.IOBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.NetworkBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.RemoteException;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.dataSource.ProductsDataSource;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.ProductDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.filters.ProductFilterState;
import com.jjak0b.android.trackingmypantry.data.services.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.services.API.ProductsList;
import com.jjak0b.android.trackingmypantry.data.services.API.Vote;
import com.jjak0b.android.trackingmypantry.data.services.API.VoteResponse;
import com.jjak0b.android.trackingmypantry.util.ResourceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductsRepository {
    private static final String TAG = "ProductsRepository";

    private static ProductsRepository instance;
    private static final Object sInstanceLock = new Object();

    private ProductsDataSource dataSource;
    private AuthRepository authRepository;
    private AppExecutors mAppExecutors;
    private PantryDB pantryDB;
    private ProductDao productDao;

    private MediatorLiveData<Resource<ProductsList>> mSearchResult;
    private LiveData<Resource<ProductsList>> mSearchResultSource;
    private LiveData<Resource<ProductsList>> mDefaultSearchResultSource;

    ProductsRepository(final Context context) {
        pantryDB = PantryDB.getInstance( context );
        productDao = pantryDB.getProductDao();
        dataSource = ProductsDataSource.getInstance(context);
        authRepository = AuthRepository.getInstance(context);
        mAppExecutors = AppExecutors.getInstance();

        mSearchResult = new MediatorLiveData<>();
        mDefaultSearchResultSource = new MutableLiveData<>(
                Resource.error(new IllegalStateException("You must perform a search first"), null)
        );
        unsetSearchResult();
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

    /**
     * Fetch a products list of suggestions searching by barcode
     * if a provided products is used, then a caller should call {@link #register(UserProduct)} on that
     * If response contains errors, then should be:
     * <ul>
     *     <li>{@link ProductsDataSource#search(String)}'s exceptions</li>
     * </ul>
     * @see ProductsDataSource#search(String)
     * @param barcode
     * @return
     */
    @MainThread
    public LiveData<Resource<List<UserProduct>>> search(String barcode) {
        Log.d(TAG, "Request product list by " + barcode );
        // Api call
        final LiveData<ApiResponse<ProductsList>> mApiResponse = dataSource.search(barcode);

        // used to store request data for future vote or register checks
        final MediatorLiveData<List<UserProduct>> searchResult = new MediatorLiveData<>();
        searchResult.setValue(new ArrayList<>(0));

        final MutableLiveData<ProductsList> mResult = new MutableLiveData<>(null);
        LiveData<Resource<ProductsList>> mResultSource = new NetworkBoundResource<ProductsList, ProductsList>(mAppExecutors) {
            @Override
            protected void saveCallResult(ProductsList item) {
                // Fix colleagues Implementation quirks
                for (UserProduct product : item.getProducts() ) {
                    String imgURI = product.getImg();
                    boolean isValid = URLUtil.isDataUrl(imgURI) || URLUtil.isValidUrl(imgURI);
                    if( imgURI != null && !isValid ) {
                        product.setImg("data:image/*;base64," + imgURI);
                    }
                }
                mResult.postValue(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable ProductsList data) {
                return true;
            }

            @Override
            protected LiveData<ProductsList> loadFromDb() {
                return mResult;
            }

            @Override
            protected LiveData<ApiResponse<ProductsList>> createCall() {
                return mApiResponse;
            }
        }.asLiveData();
        setSearchResult(mResultSource);

        return new NetworkBoundResource<List<UserProduct>, ProductsList>(mAppExecutors) {
            @Override
            protected void saveCallResult(ProductsList item) {
                searchResult.postValue(item.getProducts());
            }

            @Override
            protected boolean shouldFetch(@Nullable List<UserProduct> data) {
                return true;
            }

            @Override
            protected void onFetchFailed(Throwable cause) {
                LiveData<List<UserProduct>> mDBSource = productDao.getProductsByBarcode(barcode);
                mAppExecutors.mainThread().execute(() -> {
                    searchResult.addSource(mDBSource, searchResult::setValue);
                });
            }

            @Override
            protected LiveData<List<UserProduct>> loadFromDb() {
                return searchResult;
            }

            @Override
            protected LiveData<ApiResponse<ProductsList>> createCall() {
                return Transformations.adapt(getLastSearchResult());
            }

        }.asLiveData();
    }

    private LiveData<Resource<ProductsList>> getLastSearchResult() {
        return mSearchResult;
    }

    @MainThread
    private void unsetSearchResult() {
        // Log.d(TAG, "Clearing Search result");
        if( mSearchResultSource != null )
            mSearchResult.removeSource(mSearchResultSource);
        mSearchResultSource = mDefaultSearchResultSource;
        mSearchResult.addSource(mSearchResultSource, resource -> mSearchResult.setValue(resource));
        Log.d(TAG, "Unset search token");
    }

    @MainThread
    private void setSearchResult(LiveData<Resource<ProductsList>> mSource ) {
        if( mSource != null ){
            Log.d(TAG, "Setting new Search result");
            // detach old token
            if( mSearchResultSource != null )
                mSearchResult.removeSource(mSearchResultSource);
            // attach new token
            mSearchResultSource = mSource;
            mSearchResult.addSource(mSearchResultSource, resource -> {
                mSearchResult.setValue(resource);
            });
        }
        else {
            unsetSearchResult();
        }
    }

    private LiveData<Resource<String>> getSearchToken() {
        return Transformations.forward(getLastSearchResult(), input -> {
            return new MutableLiveData<>(Resource.success(input.getData().getToken()));
        });
    }

    private LiveData<Resource<Boolean>> isProductInSearchList(@NonNull UserProduct product) {
        return Transformations.forwardOnce(getLastSearchResult(), resourceResult -> {
            ProductsList result = resourceResult.getData();
            List<UserProduct> products = result.getProducts();
            // this list can be very long, so do check on async
            return Transformations.simulateApi(mAppExecutors.diskIO(), mAppExecutors.mainThread(), () -> {
                return product.getRemote_id() != null && products.stream().anyMatch(item -> Objects.equals(item.getRemote_id(), product.getRemote_id()));
            });
        });
    }

    /**
     * Register the provided product on remote and local.
     * if some error happens, in general then the resource's data (is null and) hasn't be registered locally  because hasn't been registered on remote
     * but if I/O error happens then will provide a locally registered product.
     * Errors that could happens on Resource are:
     * <ul>
     *     <li>{@link #add(UserProduct)}'s exceptions </li>
     *     <li>{@link #addPreference(UserProduct)}'s exceptions </li>
     * </ul>
     * @implNote  {@link #search(String)} must be called first, to fetch
     * @see #add(UserProduct)
     * @see #addPreference(UserProduct)
     * @param product
     * @return
     */
    public LiveData<Resource<UserProduct>> register(@NonNull UserProduct product) {
        final MediatorLiveData<Resource<UserProduct>> mResult = new MediatorLiveData<>();
        // Only one of the following operations will fetch on remote due to their implementations

        // add on remote if the product is not in the search list and add it locally
        // if some errors happens due to IO errors, then it could add it to local
        // so if use reuse this product, it could be fetched properly
        final LiveData<Resource<UserProduct>> mProductResult = Transformations.forwardOnce(authRepository.getLoggedAccount(), rUser -> {
            String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
            product.setUserOwnerId(ownerID);
            return add(product);
        });
        // add preference on remote if product is in the search list and add it locally
        final LiveData<Resource<VoteResponse>> mVoteResult = addPreference(product);

        final ResourceUtils.ResourcePairLiveData<UserProduct, VoteResponse> mPair =
                ResourceUtils.ResourcePairLiveData.create(mProductResult, mVoteResult);


        mResult.addSource(mPair, resourceResourcePair -> {
            // notify when both are done and so consume the search token
            if( resourceResourcePair.first.getStatus() != Status.LOADING
                && resourceResourcePair.second.getStatus() != Status.LOADING) {

                // stop listening both and detach pair source
                mResult.removeSource(mPair);
                mPair.removeSources(mProductResult, mVoteResult);

                Log.d(TAG, "got result: " + resourceResourcePair );
                // set the result value
                // mResult.setValue(resourceResourcePair.first);
                // unsetting the token here and not in #add or #addPreference because of async-ness
                // since both could require the token and could happens that one of them could toggle it while the other one is using it
                unsetSearchResult();

                // attach to product source only
                // without detaching pair and this, the unsetSearchResult will occur on each local update
                mResult.addSource(mProductResult, mResult::setValue);
            }
        });

        return mResult;
    }

    /** Add a product preference on remote.
     * Errors that could happens on Resource are:
     * <ul>
     *     <li>{@link ProductsDataSource#postPreference}'s exceptions </li>
     * </ul>
     * @see ProductsDataSource#postPreference
     * @param product
     * @return
     */
    private LiveData<Resource<VoteResponse>> addPreference(@NonNull UserProduct product ) {
        return Transformations.forward(isProductInSearchList(product), input -> {
            boolean isInList = input.getData();
            final MutableLiveData<VoteResponse> data = new MutableLiveData<>(null);
            Log.d(TAG, "Adding preference for " + product + "\nis Product on remote list: " + isInList );
            return new NetworkBoundResource<VoteResponse, VoteResponse>(mAppExecutors) {

                @Override
                protected void saveCallResult(VoteResponse item) {
                    data.postValue(item);
                    // mAppExecutors.mainThread().execute(() -> unsetSearchResult());
                }

                @Override
                protected boolean shouldFetch(@Nullable VoteResponse data) {
                    return isInList;
                }

                @Override
                protected void onFetchFailed(Throwable cause) {
                    Log.e(TAG, "Unable to vote product " + product, cause);
                }

                @Override
                protected LiveData<VoteResponse> loadFromDb() {
                    return data;
                }

                @Override
                protected LiveData<ApiResponse<VoteResponse>> createCall() {
                    return Transformations.switchMap(getLastSearchResult(), resource -> {
                        return dataSource.postPreference(new Vote(resource.getData().getToken(), product.getRemote_id(), 1));
                    });
                }
            }.asLiveData();
        });
    }

    /** Add a product entry on remote and locally if the first one succeed
     * Could ad the product locally if I/O Error happens.
     * Errors that could happens on Resource are:
     * <ul>
     *     <li>{@link ProductsDataSource#postProduct(CreateProduct)}'s exceptions </li>
     * </ul>
     *
     * @see ProductsDataSource#postProduct
     * @param product
     * @return
     */
   private LiveData<Resource<UserProduct>> add(@NonNull UserProduct product) {

       String barcode = product.getBarcode();
       String ownerID = product.getUserOwnerId();
        return Transformations.forward(isProductInSearchList(product), isInListResource -> {
            boolean isInList = isInListResource.getData();

            final MediatorLiveData<Resource<UserProduct>> mResult = new MediatorLiveData<>();

            // we add the fetched product or in specific cases we allow un-fetched products
            // into this variable so we can use later to determinate when should be added to local
            // we will use this initial product if it's in the search list or as "cache" if failed fetch due to IO
            final MutableLiveData<UserProduct> mProduct = new MutableLiveData<>(product);

            // we have the following cases
            // product not in list - fetched success -> add locally ( and on remote ) - handled by mFetchedSource
            // product not in list - fetched error ->  add locally "cache" <=> we are offline - handled by mFetchedSource
            // product in list -> add locally "cache"

            Log.d(TAG, "Adding product " + product + "\nis Product on remote list: " + isInList );
            LiveData<Resource<UserProduct>> mFetchedSource = new NetworkBoundResource<UserProduct, CreateProduct>(mAppExecutors) {

                @Override
                protected void saveCallResult(CreateProduct item) {
                    // consume token
                    // mAppExecutors.mainThread().execute(() -> { unsetSearchResult(); });
                    item.setUserOwnerId(ownerID);
                    mProduct.postValue(item);
                }

                @Override
                protected boolean shouldFetch(@Nullable UserProduct data) {
                    return !isInList;
                }

                @Override
                protected LiveData<UserProduct> loadFromDb() {
                    return mProduct;
                }

                @Override
                protected LiveData<ApiResponse<CreateProduct>> createCall() {
                    return Transformations.switchMap(getSearchToken(), tokenRes -> {
                        return dataSource.postProduct(new CreateProduct(
                                product,
                                tokenRes.getData()
                        ));
                    });
                }
            }.asLiveData();

            // setup what should see the caller
            mResult.addSource(mFetchedSource, resourceFetched -> {
                boolean shouldAddLocally = false;
                switch (resourceFetched.getStatus()) {
                    case ERROR:
                        shouldAddLocally = false;
                        Throwable cause = resourceFetched.getError();
                        // if we are offline or due to I/O errors add it locally
                        if( cause instanceof IOException) {
                            // we keep use initial "cached" value
                            Log.w(TAG, "Adding product only locally due to IOError", cause);
                            shouldAddLocally = resourceFetched.getData() != null;
                        }
                        else {
                            // it's not in the search list, so we added it previously locally in offline
                            // and remote now api provided an error.
                            // we have no knowledge about the reason of the error
                            // In particular if it's an error code 500, api don't provide the cause
                            if( cause instanceof RemoteException) {
                                Log.e(TAG, "Unable to add product to remote " + product, cause);
                            }
                            shouldAddLocally = false;
                        }
                        break;
                    case SUCCESS:
                        // add locally either when it's in the list or not
                        shouldAddLocally = resourceFetched.getData() != null;
                        break;
                    default:
                        break;
                }

                if( !shouldAddLocally ) {
                    // just forward the result
                    mResult.setValue(resourceFetched);
                }
                else {
                    // attach local live data source as live data response
                    LiveData<Resource<UserProduct>> mLocalSource = new IOBoundResource<UserProduct>(mAppExecutors) {
                        // insert to local
                        @Override
                        protected void saveCallResult(UserProduct item) {
                            productDao.updateOrInsert(item);
                        }

                        @Override
                        protected boolean shouldFetch(@Nullable UserProduct data) {
                            return true;
                        }

                        @Override
                        protected LiveData<UserProduct> loadFromDb() {
                            return productDao.get(barcode, ownerID);
                        }

                        @Override
                        protected LiveData<ApiResponse<UserProduct>> createCall() {
                            return Transformations.adapt(Transformations.simulateApi(
                                mAppExecutors.diskIO(),
                                mAppExecutors.mainThread(),
                                resourceFetched::getData
                            ));
                        }
                    }.asLiveData();

                    // detach this source
                    mResult.removeSource(mFetchedSource);
                    // attach local source
                    mResult.addSource(mLocalSource, mResult::setValue );
                }
            });

            return mResult;
        });
    }

    public LiveData<Resource<UserProduct>> get(String barcode) {
        return Transformations.forward(authRepository.getLoggedAccount(), rUser -> {
            String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
            return IOBoundResource.adapt(mAppExecutors, productDao.get(barcode, ownerID));
        });
    }

    public LiveData<Resource<ProductWithTags>> getDetails(@NonNull String barcode) {
       return Transformations.forward(authRepository.getLoggedAccount(), rUser -> {
           String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
           return IOBoundResource.adapt(mAppExecutors, productDao.getDetails(barcode, ownerID ));
       });
    }

    public LiveData<Resource<List<ProductWithTags>>> getDetails( ProductFilterState filter ){
        return Transformations.forward(authRepository.getLoggedAccount(), rUser -> {
            String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
            LiveData<List<ProductWithTags>> liveData;
            if( filter == null ){
                liveData = productDao.getAllProductsWithTags(ownerID);
            }
            else {
                liveData = productDao.getAllProductsWithTags(
                        ownerID,
                        filter.barcode != null ? "%"+filter.barcode+"%" : null,
                        filter.name != null ? "%"+filter.name+"%" : null,
                        filter.description != null ? "%"+filter.description+"%" : null,
                        filter.tagsIDs
                );
            }

            return IOBoundResource.adapt(mAppExecutors, liveData);
        });
    }

    public LiveData<Resource<ProductWithTags>> addDetails(@NonNull ProductWithTags data ) {
       return Transformations.forwardOnce(authRepository.getLoggedAccount(), rUser-> {
           String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
           LiveData<ProductWithTags> dbSource = productDao.getDetails(data.product.getBarcode(), ownerID);

           return new NetworkBoundResource<ProductWithTags, ProductWithTags>(mAppExecutors) {
                @Override
                protected void saveCallResult(ProductWithTags item) {
                    item.product.setUserOwnerId(ownerID);
                    for (ProductTag tag : item.tags ) {
                        tag.setUserId(item.product.getUserOwnerId());
                    }
                    productDao.replaceProductAssignedTags(
                            item.product,
                            item.tags
                    );
                }

                @Override
                protected boolean shouldFetch(@Nullable ProductWithTags data) {
                    return true;
                }

                @Override
                protected LiveData<ProductWithTags> loadFromDb() {
                    return dbSource;
                }

                @Override
                protected LiveData<ApiResponse<ProductWithTags>> createCall() {
                    return Transformations.adapt(new MutableLiveData<>(Resource.success(data)));
                }
           }.asLiveData();
       });
    }

    public LiveData<Resource<UserProduct>> remove(@NonNull UserProduct product) {
        return Transformations.forwardOnce(authRepository.getLoggedAccount(), rUser -> {
            String userId = rUser.getData() != null ? rUser.getData().getId() : null;
            String barcode = product.getBarcode();
            LiveData<Resource<UserProduct>> mProductSource = get(barcode);
            return Transformations.forwardOnce(mProductSource, rProduct -> {
                UserProduct data = rProduct.getData();
                final MutableLiveData<UserProduct> mResult = new MutableLiveData<>(data);

                boolean shouldFetch = data != null
                        && data.getRemote_id() != null
                        && Objects.equals( data.getUserCreatorId(), userId );

                Log.d(TAG, "Removing product from local and remote");
                // else remove also on remote
                return new NetworkBoundResource<UserProduct, UserProduct>(mAppExecutors) {

                    @Override
                    protected void saveCallResult(UserProduct item) {
                        item.setUserOwnerId(userId);
                        mResult.postValue(item);
                        productDao.remove(item);
                    }

                    @Override
                    protected boolean shouldFetch(@Nullable UserProduct data) {
                        return data != null;
                    }

                    @Override
                    protected void onFetchFailed(Throwable cause) {
                        productDao.remove(data);
                    }


                    @Override
                    protected LiveData<UserProduct> loadFromDb() {
                        return mResult;
                    }

                    @Override
                    protected LiveData<ApiResponse<UserProduct>> createCall() {
                        if( shouldFetch )
                            return dataSource.delete(data.getRemote_id());
                        else
                            return Transformations.adapt(mProductSource);
                    }
                }.asLiveData();
            });
        });
    }

    public LiveData<Resource<List<ProductTag>>> getTags() {
        return Transformations.forward(authRepository.getLoggedAccount(), rUser -> {
            String userId = rUser.getData() != null ? rUser.getData().getId() : null;
            return IOBoundResource.adapt(mAppExecutors, productDao.getAllTags(userId));
        });
    }
}
