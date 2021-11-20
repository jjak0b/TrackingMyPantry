package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductInstanceGroupInfo;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.db.relationships.PurchaseInfoWithPlace;
import com.jjak0b.android.trackingmypantry.data.repositories.PlacesRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.PantriesRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.PurchasesRepository;
import com.jjak0b.android.trackingmypantry.data.services.API.CreateProduct;

public class _RegisterProductViewModel extends AndroidViewModel {

    private ProductsRepository productsRepo;
    private PantriesRepository pantryRepo;
    private PlacesRepository placesRepo;
    private PurchasesRepository purchasesRepo;

    private MutableLiveData<Resource<ProductWithTags>> mProductDetails;
    private MutableLiveData<Resource<ProductInstanceGroupInfo>> mProductGroupDetails;
    private MutableLiveData<Resource<PurchaseInfoWithPlace>> mProductPurchaseDetails;

    private LiveEvent<Boolean> onSave;
    private LiveEvent<Boolean> onCanSubmit;

    public _RegisterProductViewModel(@NonNull Application application) {
        super(application);
        productsRepo = ProductsRepository.getInstance(application);
        pantryRepo = PantriesRepository.getInstance(application);
        purchasesRepo = PurchasesRepository.getInstance(application);

        mProductDetails = new MutableLiveData<>(Resource.loading(null));
        mProductGroupDetails = new MutableLiveData<>(Resource.loading(null));
        mProductPurchaseDetails = new MutableLiveData<>(Resource.loading(null));
        // mProductChoice = new MutableLiveData<>();

        onSave = new LiveEvent<>();
        onCanSubmit = new LiveEvent<>();
        initCanSubmit();
    }

    public void setBaseProduct() {

    }

    public void setupNew() {

    }

    private void initCanSubmit() {

        LiveData<?>[] required = new LiveData<?>[] { mProductDetails, mProductGroupDetails, mProductPurchaseDetails };
        MutableLiveData<Integer> readySource = new MutableLiveData<>(0);

        Resource<?>[] results = new Resource<?>[required.length];
        // assert to have them all collected, counts how many data we got and notify true when we git them all
        int index = 0;
        for (LiveData<?> _source : required ) {
            LiveData<Resource<?>> source = (LiveData<Resource<?>>) _source;
            int tmpIndex = index;
            onCanSubmit.addSource(source, new Observer<Resource<?>>() {
                private final LiveData<Resource<?>> __source = source;
                @Override
                public void onChanged(Resource<?> resource) {
                    switch (resource.getStatus()) {
                        case SUCCESS:
                            // we got a result
                            readySource.setValue(-1);
                            break;
                        case ERROR:
                            // we got an error
                            readySource.setValue(1);
                            break;
                        default:
                            break;
                    }
                }
            });
            ++index;
        }

        onCanSubmit.addSource(readySource, new Observer<Integer>() {
            int count = required.length;
            @Override
            public void onChanged(Integer value) {
                count += value;
                if( count <= 0) {
                    onCanSubmit.setValue(true);
                }
                else {
                    onCanSubmit.setValue(false);
                }
            }
        });
    }

    public LiveData<Boolean> onSave() {
        return onSave;
    }

    public void save() {
        onSave.setValue(true);
        onSave.postValue(false);
    }

    public LiveData<Boolean> onCanSubmit() {
        return onCanSubmit;
    }

    public void setProductDetails(Resource<ProductWithTags> data ) {
        mProductDetails.setValue(data);
    }

    public void setProductInstanceDetails( Resource<ProductInstanceGroupInfo> data ) {
        mProductGroupDetails.setValue(data);
    }

    public void setProductPurchaseDetails( Resource<PurchaseInfoWithPlace> data ) {
        mProductPurchaseDetails.setValue(data);
    }

    public LiveData<Resource<Void>> submit() {


        MutableLiveData<Integer> readySource = new MediatorLiveData<>();
        MediatorLiveData<Resource<Void>> mResult = new MediatorLiveData<>();
        mResult.setValue(Resource.loading(null));

        LiveData<Resource<ProductWithTags>> mProductResult = addProductDetails();
        LiveData<Resource<ProductInstanceGroup>> mProductGroupResult = addProductGroupDetails(mProductResult);
        LiveData<Resource<PurchaseInfo>> mProductPurchaseResult = addProductPurchaseDetails(mProductResult);

        LiveData<?>[] required = new LiveData<?>[] { mProductResult, mProductGroupResult, mProductPurchaseResult };
        Resource<?>[] results = new Resource<?>[required.length];
        // assert to have them all collected
        int index = 0;
        for (LiveData<?> _source : required ) {
            LiveData<Resource<?>> source = (LiveData<Resource<?>>) _source;
            int tmpIndex = index;
            mResult.addSource(source, new Observer<Resource<?>>() {
                private final LiveData<Resource<?>> __source = source;
                private final int __index = tmpIndex;
                @Override
                public void onChanged(Resource<?> resource) {
                    results[__index] = resource;
                    switch (resource.getStatus()) {
                        case SUCCESS:
                            mResult.removeSource(__source);
                            // we got a result
                            readySource.setValue(__index);
                            break;
                        case ERROR:
                            mResult.removeSource(readySource);
                            mResult.removeSource(__source);
                            // we got an error, we will report it
                            mResult.setValue(Resource.error(resource.getError(), null));
                            break;
                        default:
                            break;
                    }
                }
            });
            ++index;
        }
        mResult.addSource(readySource, new Observer<Integer>() {
            int steps = required.length;
            @Override
            public void onChanged(Integer index) {
                switch (results[index].getStatus()) {
                    case SUCCESS:
                        --steps;
                        break;
                    case ERROR:
                        mResult.removeSource(readySource);
                        break;
                }
                if( steps <= 0) {

                    mResult.removeSource(readySource);
                    mResult.setValue(Resource.success(null));
                }
            }
        });

        return mResult;
    }

    private LiveData<Resource<ProductWithTags>> addProductDetails() {
        return Transformations.forward(mProductDetails, mProductResource -> {
            return productsRepo.add(mProductResource.getData());
        });
    }

    private LiveData<Resource<ProductInstanceGroup>> addProductGroupDetails(@NonNull LiveData<Resource<ProductWithTags>> mProduct) {
        return Transformations.forward(mProduct, mProductResource -> {
            ProductWithTags productWithTags = mProductResource.getData();

            return Transformations.forward( mProductGroupDetails, mProductGroupResource -> {
                ProductInstanceGroupInfo groupInfo = mProductGroupResource.getData();
                groupInfo.group.setProductId(productWithTags.product.getId());

                return Transformations.forward(pantryRepo.add(groupInfo.pantry), mPantryResource -> {
                    Pantry pantry = mPantryResource.getData();
                    groupInfo.group.setPantryId(pantry.getId());

                    return pantryRepo.add(groupInfo.group);
                });
            });
        });
    }

    public LiveData<Resource<PurchaseInfo>> addProductPurchaseDetails(@NonNull LiveData<Resource<ProductWithTags>> mProduct) {
        return Transformations.forward(mProduct, mProductResource -> {
            Product product = mProductResource.getData().product;

            return Transformations.forward( mProductPurchaseDetails, mDetailsResource -> {
                PurchaseInfoWithPlace details = mDetailsResource.getData();
                details.info.setProductId(product.getId());

                // place is optional
                LiveData<Resource<Place>> mPlaceAdded = details.place == null
                        ? new MutableLiveData<>(Resource.success(null))
                        : placesRepo.add(details.place);

                return Transformations.forward(mPlaceAdded, mPlaceResource -> {
                    Place place = mPlaceResource.getData();
                    details.info.setPlaceId(place != null ? place.getId() : null );

                    return purchasesRepo.add(details.info);
                });
            });
        });
    }
}
