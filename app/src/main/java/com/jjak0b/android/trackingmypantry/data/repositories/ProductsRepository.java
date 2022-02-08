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
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * if a provided products is used, then a caller should call {@link #register(Product)} on that
     * If response contains errors, then should be:
     * <ul>
     *     <li>{@link ProductsDataSource#search(String)}'s exceptions</li>
     * </ul>
     * @see ProductsDataSource#search(String)
     * @param barcode
     * @return
     */
    @MainThread
    public LiveData<Resource<List<? extends Product>>> search(String barcode) {
        Log.d(TAG, "Request product list by " + barcode );

        // First we search and fetch results
        final MutableLiveData<ProductsList> mResult = new MutableLiveData<>(null);
        LiveData<Resource<ProductsList>> mResultSource = new NetworkBoundResource<ProductsList, ProductsList>(mAppExecutors) {
            @Override
            protected void saveCallResult(ProductsList item) {
                // Fix colleagues Implementation quirks
                for (Product product : item.getProducts() ) {
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
                return dataSource.search(barcode);
            }
        }.asLiveData();
        setSearchResult(mResultSource);

        LiveData<Resource<ProductsList>> mSearchResult = getLastSearchResult();

        // And then observe the search result, and forward results or fallback

        // setup fallback source
        final MediatorLiveData<List<UserProduct>> dbAdapter = new MediatorLiveData<>();
        dbAdapter.addSource(get(barcode), resource -> {
            if( resource.getStatus() != Status.LOADING ){
                if( resource.getData() != null )
                    dbAdapter.setValue(Collections.singletonList(resource.getData()));
                else
                    dbAdapter.setValue(Collections.emptyList());
            }
        });

        // used to store search result data or a fallback list
        final MediatorLiveData<List<? extends Product>> fakeDBAdapter = new MediatorLiveData<>();
        fakeDBAdapter.addSource(dbAdapter, fakeDBAdapter::setValue );

        LiveData<Resource<List<? extends Product>>> mItems = new NetworkBoundResource<List<? extends Product>, ProductsList>(mAppExecutors) {
            @Override
            protected void saveCallResult(ProductsList item) {
                // detach fallback source
                mAppExecutors.mainThread().execute(() -> {
                    fakeDBAdapter.removeSource(dbAdapter);
                });
                fakeDBAdapter.postValue(item.getProducts());
            }

            @Override
            protected boolean shouldFetch(@Nullable List<? extends Product> data) {
                return true;
            }

            @Override
            protected LiveData<List<? extends Product>> loadFromDb() {
                return fakeDBAdapter;
            }

            @Override
            protected LiveData<ApiResponse<ProductsList>> createCall() {
                return Transformations.adapt(mSearchResult);
            }

        }.asLiveData();

        // return mItems;
        return manipulateOwnedProducts(barcode, mItems);
    }

    private LiveData<Resource<List<? extends Product>>> manipulateOwnedProducts(String barcode, LiveData<Resource<List<? extends Product>>> source) {

        final LiveData<Resource<UserProduct>> mUser = get(barcode);

        return Transformations.forwardOnce(mUser, rProduct -> {
            UserProduct productOwnedByUser = rProduct.getData();

            Log.d(TAG, "Product Owned by user: " + productOwnedByUser );
            // Now there is a problem: can't use forward api because it make null any data provided with (and if any) error occurred.
            // so using an IOBoundResource adapter to recover that error from original source

            final MediatorLiveData<List<? extends Product>> fakeDBAdapter = new MediatorLiveData<>();
            fakeDBAdapter.addSource(source, listResource -> {
                // Log.d(TAG, "source default" + listResource );
                if( listResource.getStatus() != Status.LOADING ) {
                    fakeDBAdapter.setValue(listResource.getData());
                }
            });

            return new IOBoundResource<List<? extends Product>>(mAppExecutors) {

                @Override
                protected void saveCallResult(List<? extends Product> items) {
                    // User owns a product
                    // So search the matching one with the (remote) provided list (if any) and move it to first position
                    List<Product> empty = new ArrayList<>(0);
                    List<Product> list = Stream.concat(empty.stream(), items.stream() )
                            .collect(Collectors.toList());

                    // search into result and replace the product with its upcast
                    if( productOwnedByUser.getRemote_id() != null ) {
                        for (ListIterator<Product> it = list.listIterator(); it.hasNext(); ) {
                            Product item = it.next();
                            if( Objects.equals(productOwnedByUser.getRemote_id(), item.getRemote_id())) {
                                it.remove();
                                break;
                            }
                        }
                        // if not found ( has been removed on remote or hasn't been added on remote yet )
                    }


                    // then add as first element
                    list.add(0, productOwnedByUser);

                    fakeDBAdapter.postValue(list);
                }

                @Override
                protected void onFetchFailed(Throwable cause) {
                    // attach fallback source
                    mAppExecutors.mainThread().execute(() -> {
                        fakeDBAdapter.addSource(source, r -> {
                            // Log.d(TAG, "source fallback" + r );
                            if( r.getStatus() != Status.LOADING ) fakeDBAdapter.setValue(r.getData());
                        });
                    });
                }

                @Override
                protected boolean shouldFetch(@Nullable List<? extends Product> data) {
                    boolean shouldFetch = productOwnedByUser != null;
                    if( shouldFetch ) {
                        // detach fallback source
                        fakeDBAdapter.removeSource(source);
                    }

                    return shouldFetch;
                }

                @Override
                protected LiveData<List<? extends Product>> loadFromDb() {
                    return fakeDBAdapter;
                }

                @Override
                protected LiveData<ApiResponse<List<? extends Product>>> createCall() {
                    return Transformations.adapt(source);
                }
            }.asLiveData();
        });
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

    private LiveData<Resource<Boolean>> isProductInSearchList(@NonNull Product product) {
        return Transformations.forwardOnce(getLastSearchResult(), resourceResult -> {
            ProductsList result = resourceResult.getData();
            List<Product> products = result.getProducts();
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
     *     <li>{@link #addPreference(Product)}'s exceptions </li>
     * </ul>
     * @implNote  {@link #search(String)} must be called first, to fetch
     * @see #add(UserProduct)
     * @see #addPreference(Product)
     * @param product
     * @return
     */
    public LiveData<Resource<UserProduct>> register(@NonNull Product product) {
        final MediatorLiveData<Resource<UserProduct>> mResult = new MediatorLiveData<>();
        // Only one of the following operations will fetch on remote due to their implementations

        // add on remote if the product is not in the search list and add it locally
        // if some errors happens due to IO errors, then it could add it to local
        // so if use reuse this product, it could be fetched properly
        final LiveData<Resource<UserProduct>> mProductResult = Transformations.forwardOnce(authRepository.getLoggedAccount(), rUser -> {
            String ownerID = rUser.getData() != null ? rUser.getData().getId() : null;
            return add(new UserProduct(product, ownerID));
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

                Log.d(TAG, "Register product results: " + resourceResourcePair );
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
    private LiveData<Resource<VoteResponse>> addPreference(@NonNull Product product ) {
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
       LiveData<Resource<Boolean>> mIsInList = androidx.lifecycle.Transformations.map(isProductInSearchList(product), isInListResource -> {
           // force to return a dummy null value when last search result got an error for any reason, so we use local results
           if (isInListResource.getStatus() == Status.ERROR) return Resource.success(null);
           return isInListResource;
       });
        return Transformations.forward(mIsInList, isInListResource -> {
            // this var is used when isProductInSearchList got an error, caused by last search result
            // then the following mFetchedSource will provide that error, but with the product fallback
            boolean shouldForceFetchToFail = isInListResource.getData() == null;

            boolean isInList = isInListResource.getData() != null && isInListResource.getData();

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
                    mProduct.postValue(new UserProduct(item, ownerID));
                }

                @Override
                protected boolean shouldFetch(@Nullable UserProduct data) {
                    return shouldForceFetchToFail || !isInList;
                }

                @Override
                protected LiveData<UserProduct> loadFromDb() {
                    return mProduct;
                }

                @Override
                protected LiveData<ApiResponse<CreateProduct>> createCall() {
                    // if isProductInSearchList returned an error, then also getSearchToken() will
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
                        Log.e(TAG, "Product remote registration failed ...", cause);
                        // if we are offline or due to I/O errors add it locally
                        if( cause instanceof IOException) {
                            // we keep use initial "cached" value
                            shouldAddLocally = resourceFetched.getData() != null;
                            Log.w(TAG, "Adding product only locally due to IOError: " + shouldAddLocally );
                        }
                        else {
                            // it's not in the search list, so we added it previously locally in offline
                            // and remote now api provided an error.
                            // we have no knowledge about the reason of the error
                            // In particular if it's an error code 500, api don't provide the cause
                            if( cause instanceof RemoteException) {
                                Log.e(TAG, "Unable to add product to remote " + product);
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
                    if( resourceFetched.getStatus() == Status.ERROR )
                        mResult.setValue(Resource.error(mFetchedSource.getValue().getError(),null ));
                    else
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
            LiveData<Resource<UserProduct>> mUserProductSource = get(barcode);

            return Transformations.forwardOnce(mUserProductSource, rProduct -> {
                UserProduct data = rProduct.getData();
                final MutableLiveData<UserProduct> mResult = new MutableLiveData<>(data);
                final MutableLiveData<Resource<Product>> mProductSource = new MutableLiveData<>(Resource.success(data));

                boolean shouldFetch = data != null
                        && data.getRemote_id() != null
                        && Objects.equals( data.getUserCreatorId(), userId );

                Log.d(TAG, "Removing product from local and remote");
                // else remove also on remote
                return new NetworkBoundResource<UserProduct, Product>(mAppExecutors) {

                    @Override
                    protected void saveCallResult(Product item) {
                        UserProduct p = new UserProduct(item, userId);
                        mResult.postValue(p);
                        productDao.remove(p);
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
                    protected LiveData<ApiResponse<Product>> createCall() {
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
