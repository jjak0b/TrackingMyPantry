package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductInstanceGroupInfo;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.db.relationships.PurchaseInfoWithPlace;
import com.jjak0b.android.trackingmypantry.data.repositories.PantriesRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.PlacesRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.PurchasesRepository;
import com.jjak0b.android.trackingmypantry.ui.util.Savable;

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
    private LiveEvent<Boolean> onBaseProductSet;

    private Savable<ProductWithTags> savableProductDetails;
    private Savable<ProductInstanceGroup> savableProductInfoDetails;
    private Savable<PurchaseInfoWithPlace> savableProductPurchaseDetails;

    public _RegisterProductViewModel(@NonNull Application application) {
        super(application);
        productsRepo = ProductsRepository.getInstance(application);
        pantryRepo = PantriesRepository.getInstance(application);
        purchasesRepo = PurchasesRepository.getInstance(application);
        placesRepo = PlacesRepository.getInstance(application);

        mProductDetails = new MutableLiveData<>(Resource.loading(null));
        mProductGroupDetails = new MutableLiveData<>(Resource.loading(null));
        mProductPurchaseDetails = new MutableLiveData<>(Resource.loading(null));
        // mProductChoice = new MutableLiveData<>();

        savableProductDetails = new Savable<>();
        savableProductInfoDetails = new Savable<>();
        savableProductPurchaseDetails = new Savable<>();


        onBaseProductSet = new LiveEvent<>();
        onBaseProductSet.setValue(false);

        onSave = new LiveEvent<>();
        onCanSubmit = new LiveEvent<>();

        onBaseProductSet.addSource(mProductDetails, resource -> {
            onBaseProductSet.setValue(resource.getStatus() == Status.SUCCESS);
        });
    }

    public void setupNew() {

    }

    public void saveProductDetails() {
        savableProductDetails.save();
    }

    public void saveProductInfoDetails() {
        savableProductInfoDetails.save();
    }

    public void saveProductPurchaseDetails() {
        savableProductPurchaseDetails.onSave();
    }

    public MediatorLiveData<Boolean> onSaveProductDetails() {
        return savableProductDetails.onSave();
    }

    public MediatorLiveData<Boolean> onSaveInfoDetails() {
        return savableProductInfoDetails.onSave();
    }

    public MediatorLiveData<Boolean> onSavePurchaseDetails() {
        return savableProductPurchaseDetails.onSave();
    }

    public LiveEvent<Boolean> onBaseProductSet() {
        return onBaseProductSet;
    }

    public LiveData<Resource<ProductWithTags>> getProductDetails() {
        return mProductDetails;
    }

    public LiveData<Resource<ProductInstanceGroupInfo>> getProductGroupDetails() {
        return mProductGroupDetails;
    }

    public LiveData<Resource<PurchaseInfoWithPlace>> getProductPurchaseDetails() {
        return mProductPurchaseDetails;
    }

    public void setProductDetails(Resource<ProductWithTags> resource) {
        mProductDetails.setValue(resource);
    }

    public void setInfoDetails(Resource<ProductInstanceGroupInfo> resource) {
        mProductGroupDetails.setValue(resource);
    }

    public void setPurchaseDetails(Resource<PurchaseInfoWithPlace> resource) {
        mProductPurchaseDetails.setValue(resource);
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
