package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.app.Application;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductInstanceGroupInfo;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.db.relationships.PurchaseInfoWithPlace;
import com.jjak0b.android.trackingmypantry.data.repositories.PantriesRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.PlacesRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;
import com.jjak0b.android.trackingmypantry.data.repositories.PurchasesRepository;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;
import com.jjak0b.android.trackingmypantry.ui.util.ISavable;
import com.jjak0b.android.trackingmypantry.ui.util.Savable;
import com.jjak0b.android.trackingmypantry.util.ResourceUtils;

public class RegisterProductViewModel extends AndroidViewModel implements ISavable<Boolean> {

    private static final String TAG = "RegisterProductViewModel";
    private ProductsRepository productsRepo;
    private PantriesRepository pantryRepo;
    private PlacesRepository placesRepo;
    private PurchasesRepository purchasesRepo;

    private MutableLiveData<Resource<ProductWithTags>> mProductDetails;
    private MutableLiveData<Resource<ProductInstanceGroupInfo>> mProductGroupDetails;
    private MutableLiveData<Resource<PurchaseInfoWithPlace>> mProductPurchaseDetails;


    private LiveEvent<Boolean> onBaseProductSet;
    private LiveEvent<Boolean> onReset;

    private Savable<ProductWithTags> savableProductDetails;
    private Savable<ProductInstanceGroup> savableProductInfoDetails;
    private Savable<PurchaseInfoWithPlace> savableProductPurchaseDetails;
    private Savable<Boolean> savable;

    public RegisterProductViewModel(@NonNull Application application) {
        super(application);
        productsRepo = ProductsRepository.getInstance(application);
        pantryRepo = PantriesRepository.getInstance(application);
        purchasesRepo = PurchasesRepository.getInstance(application);
        placesRepo = PlacesRepository.getInstance(application);

        mProductDetails = new MutableLiveData<>(Resource.loading(null));
        mProductGroupDetails = new MutableLiveData<>(Resource.loading(null));
        mProductPurchaseDetails = new MutableLiveData<>(Resource.loading(null));


        savableProductDetails = new Savable<>();
        savableProductInfoDetails = new Savable<>();
        savableProductPurchaseDetails = new Savable<>();


        onBaseProductSet = new LiveEvent<>();
        onBaseProductSet.setValue(false);
        onReset = new LiveEvent<>();
        onReset.setValue(false);

        onBaseProductSet.addSource(mProductDetails, resource -> {
            onBaseProductSet.setValue(resource.getStatus() == Status.SUCCESS);
        });
        savable = new Savable<>();
    }

    public void setupNew() {
        Log.d(TAG, "Reset request for new product");
        setProductDetails(Resource.loading(null));
        setInfoDetails(Resource.loading(null));
        setPurchaseDetails(Resource.loading(null));
        onReset.setValue(true);
        onReset.postValue(false);
    }

    public LiveData<Boolean> onReset() {
        return onReset;
    }

    public LiveData<Resource<UserProduct>> getMyProduct(String barcode) {
        return productsRepo.get(barcode);
    }

    public void saveProductDetails() {
        savableProductDetails.save();
    }

    public void saveProductInfoDetails() {
        savableProductInfoDetails.save();
    }

    public void saveProductPurchaseDetails() {
        savableProductPurchaseDetails.save();
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
        updateValidity(true);
    }

    public void setInfoDetails(Resource<ProductInstanceGroupInfo> resource) {
        mProductGroupDetails.setValue(resource);
        updateValidity(true);
    }

    public void setPurchaseDetails(Resource<PurchaseInfoWithPlace> resource) {
        mProductPurchaseDetails.setValue(resource);
        updateValidity(true);
    }

    private boolean updateValidity(boolean updateSavable) {
        boolean isValid = true;
        isValid = isValid && Transformations.onValid(mProductDetails.getValue(), null);
        isValid = isValid && Transformations.onValid(mProductGroupDetails.getValue(), null);
        isValid = isValid && Transformations.onValid(mProductPurchaseDetails.getValue(), null);

        if( updateSavable )
            savable.enableSave(isValid);
        return isValid;
    }

    private LiveData<Resource<ProductWithTags>> addProductDetails() {
        return Transformations.forwardOnce(mProductDetails, mProductResource -> {
            Log.d(TAG, "Adding Details" + mProductResource.getData() );
            return productsRepo.addDetails(mProductResource.getData());
        });
    }

    private LiveData<Resource<ProductInstanceGroup>> addProductGroupDetails(@NonNull LiveData<Resource<ProductWithTags>> mProduct) {

        LiveData<Resource<Pair<ProductWithTags, ProductInstanceGroupInfo>>> mPair =
                ResourceUtils.ResourcePairLiveData
                        .create(mProduct, mProductGroupDetails)
                        .asCombinedLiveData();

        // Note:
        // while saving, other tabs may override/make null their respective details because the repository sources are updating.
        // so forwarding this pair only once allow to ignore that overrides, and to consider only first valid value
        return Transformations.forwardOnce(mPair, rPair -> {
            ProductWithTags productWithTags = rPair.getData().first;
            ProductInstanceGroupInfo groupInfo = rPair.getData().second;

            groupInfo.product = productWithTags.product;
            groupInfo.group.setProductId(groupInfo.product.getBarcode());
            Log.d(TAG, "Adding group " + groupInfo.group );
            Log.d(TAG, "Adding pantry " +  groupInfo.pantry != null ? groupInfo.pantry.getId() + " " + groupInfo.pantry.getName() : null );
            return Transformations.forwardOnce(pantryRepo.addGroup(groupInfo.group, groupInfo.product, groupInfo.pantry), resourceGroupID -> {
                Log.d(TAG, "Added group and pantry" );
                return pantryRepo.getGroup(resourceGroupID.getData());
            });
        });
    }

    public LiveData<Resource<PurchaseInfo>> addProductPurchaseDetails(@NonNull LiveData<Resource<ProductWithTags>> mProduct) {

        LiveData<Resource<Pair<ProductWithTags, PurchaseInfoWithPlace>>> mPair =
                ResourceUtils.ResourcePairLiveData
                        .create(mProduct, mProductPurchaseDetails)
                        .asCombinedLiveData();

        // Note:
        // while saving, other tabs may override/make null their respective details because the repository sources are updating.
        // so forwarding this pair only once allow to ignore that overrides, and to consider only first valid value
        return Transformations.forwardOnce(mPair, rPair -> {
            UserProduct product =  rPair.getData().first.product;
            PurchaseInfoWithPlace details = rPair.getData().second;
            details.info.setProductId(product.getBarcode());

            Log.d(TAG, "Adding place if set: " + details.place );
            // place is optional
            LiveData<Resource<Place>> mPlaceAdded = details.place == null
                    ? new MutableLiveData<>(Resource.success(null))
                    : placesRepo.add(details.place);

            return Transformations.forwardOnce(mPlaceAdded, mPlaceResource -> {
                Place place = mPlaceResource.getData();
                if( place != null ) {
                    Log.d(TAG, "Added place " + details.place );
                    details.info.setPlaceId(place.getId());
                }
                else {
                    details.info.setPlaceId(null);
                }

                details.place = place;
                Log.d(TAG, "Adding group info " + details.info );

                return purchasesRepo.add(details.info);
            });
        });
    }

    @Override
    public LiveData<Boolean> canSave() {
        return savable.canSave();
    }

    @Override
    public void saveComplete() {
        savable.saveComplete();
    }

    @Override
    public void save() {

        savable.onSaved().removeSource(savable.onSave());
        savable.onSaved().addSource(savable.onSave(), aBoolean -> {
            if( aBoolean ){
                savable.setSavedResult(Resource.loading(null));
                return;
            }
            savable.onSaved().removeSource(savable.onSave());

            if( updateValidity(false) ) {
                LiveData<Resource<ProductWithTags>> add1 = addProductDetails();
                LiveData<Resource<ProductInstanceGroup>> add2 = addProductGroupDetails(add1);
                LiveData<Resource<PurchaseInfo>> add3 = addProductPurchaseDetails(add1);

                ResourceUtils.ResourcePairLiveData<ProductInstanceGroup, PurchaseInfo> mPair =
                    ResourceUtils.ResourcePairLiveData.create(add2, add3);

                savable.onSaved().addSource(mPair, resourcePair -> {
                    if( resourcePair.first.getStatus() != Status.LOADING
                    && resourcePair.second.getStatus() != Status.LOADING ) {

                        boolean isSuccessful = resourcePair.first.getStatus() == Status.SUCCESS
                                && resourcePair.second.getStatus() == Status.SUCCESS;

                        savable.onSaved().removeSource(mPair);
                        if( isSuccessful ) {
                            savable.setSavedResult(Resource.success(true));
                        }
                        else {
                            savable.setSavedResult(Resource.error(
                                    resourcePair.first.getStatus() == Status.ERROR
                                            ? resourcePair.first.getError()
                                            : resourcePair.second.getError(),
                                    false
                            ));
                        }
                    }
                });
            }
            else {
                savable.setSavedResult(Resource.error(
                        new FormException(getApplication().getString(R.string.form_error_invalid)),
                        false)
                );
            }
        });

        savable.save();
    }

    @Override
    public MediatorLiveData<Boolean> onSave() {
        return savable.onSave();
    }

    @Override
    public MediatorLiveData<Resource<Boolean>> onSaved() {
        return savable.onSaved();
    }
}
