package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.app.Application;
import android.text.TextUtils;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;
import com.jjak0b.android.trackingmypantry.ui.util.ISavable;
import com.jjak0b.android.trackingmypantry.ui.util.Savable;
import com.jjak0b.android.trackingmypantry.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SectionProductDetailsViewModel extends AndroidViewModel implements ISavable<ProductWithTags>{

    private MutableLiveData<Resource<String>> mBarcode;
    private MutableLiveData<Resource<UserProduct>> mProduct;
    private MediatorLiveData<Resource<List<ProductTag>>> mAssignedTags;
    private LiveData<Resource<List<ProductTag>>> mSuggestionsTags;
    private AppExecutors appExecutors;
    private ProductsRepository productsRepository;
    private Savable<ProductWithTags> savable;
    private LiveData<Resource<List<ProductTag>>> mProductTagsDefaultSource;
    public SectionProductDetailsViewModel(Application application) {
        super(application);
        productsRepository = ProductsRepository.getInstance(application);
        appExecutors = AppExecutors.getInstance();
        savable = new Savable<>();
        mBarcode = new MutableLiveData<>(Resource.loading(null));
        mProduct = new MutableLiveData<>(Resource.loading(null));
        mProductTagsDefaultSource = new MutableLiveData<>(Resource.success(new ArrayList<>(0)));
        mAssignedTags = new MediatorLiveData<>();
        mAssignedTags.setValue(mProductTagsDefaultSource.getValue());
        mSuggestionsTags = productsRepository.getTags();

        // override assigned tags on barcode change, with the ones assigned to existing product
        LiveData<Resource<ProductWithTags>> mDetails = Transformations.forward(getBarcode(), resource -> {
            String barcode = resource.getData();
            return productsRepository.getDetails(barcode);
        });
        mAssignedTags.addSource(mDetails, resource -> {
            if( resource.getStatus() == Status.SUCCESS ) {
                if( resource.getData() != null)
                    setAssignedTags(resource.getData().tags);
                else
                    setAssignedTags(mProductTagsDefaultSource.getValue().getData());
            }
        });

        savable.enableSave(false);
        reset();
    }

    @Override
    protected void onCleared() {
        savable.onCleared();
        super.onCleared();
    }


    public void reset() {
        setBarcode(null);
        setProduct((UserProduct) null);
        setAssignedTags(new ArrayList<>(0));
    }

    public boolean isProductSet() {
        return Transformations.onValid(getProduct().getValue(), null);
    }

    public LiveData<Resource<String>> getBarcode() {
        return mBarcode;
    }

    public void setBarcode(String barcode) {
        if(!Objects.equals(barcode, mBarcode.getValue().getData())) {
            mBarcode.setValue(Resource.loading(barcode));
            barcode = barcode != null ? barcode.trim() : null;
            Throwable error = null;

            if(TextUtils.isEmpty(barcode)){
                error = new FormException(getApplication().getString(R.string.field_error_empty));
            }

            if( error != null ) {
                mBarcode.setValue(Resource.error(error, barcode ));
            }
            else {
                mBarcode.setValue(Resource.success(barcode));
            }
            updateValidity(true);
        }
    }

    public LiveData<Resource<UserProduct>> getProduct() {
        return mProduct;
    }

    public void setProduct(UserProduct product) {
        if(!Objects.equals(product, mProduct.getValue().getData())) {
            Throwable error = null;
            if( product == null ) {
                mProduct.setValue(Resource.loading(null));
            }
            else {
                mProduct.setValue(Resource.success(product));
            }
        }
        updateValidity(true);
    }

    public void setProduct(ProductWithTags productWithTags) {
        if( productWithTags != null){
            setProduct(productWithTags.product);
            setAssignedTags(productWithTags.tags);
        }
        else {
            setProduct((UserProduct) null);
            setAssignedTags(new ArrayList<>(0));
        }
    }

    public LiveData<Resource<List<ProductTag>>> getAssignedTags() {
        return mAssignedTags;
    }

    public void setAssignedTags(List<ProductTag> tags ) {
        if(!Objects.equals(tags, mAssignedTags.getValue().getData())) {
            mAssignedTags.setValue(Resource.loading(tags));
            mAssignedTags.setValue(Resource.success(tags));
            updateValidity(true);
        }
    }

    public LiveData<Resource<List<ProductTag>>> getSuggestionTags() {
        return mSuggestionsTags;
    }

    public void resetProduct() {
        setProduct((ProductWithTags) null);
    }

    private boolean updateValidity(boolean updateSavable) {
        boolean isValid = true;

        isValid = isValid && Transformations.onValid(getBarcode().getValue(), null);
        isValid = isValid && Transformations.onValid(getProduct().getValue(), null);
        isValid = isValid && Transformations.onValid(getAssignedTags().getValue(), null);

        if( updateSavable )
            savable.enableSave(isValid);
        return isValid;
    }

    public LiveData<Resource<UserProduct>> searchMyProducts(String barcode) {
        return productsRepository.get(barcode);
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
        savable.onSaved().addSource(savable.onSave(), isSaving -> {
            if (isSaving) {
                savable.setSavedResult(Resource.loading(null));
                return;
            }
            savable.onSaved().removeSource(savable.onSave());


            ResourceUtils.ResourcePairLiveData<UserProduct, List<ProductTag>> mPair =
                    ResourceUtils.ResourcePairLiveData.create(getProduct(), getAssignedTags() );

            savable.onSaved().addSource(mPair, resourceResourcePair -> {
                if( resourceResourcePair.first.getStatus() != Status.LOADING
                    && resourceResourcePair.second.getStatus() != Status.LOADING ) {

                    savable.onSaved().removeSource(mPair);

                    boolean isValid = resourceResourcePair.first.getStatus() == Status.SUCCESS
                            && resourceResourcePair.second.getStatus() == Status.SUCCESS
                            && updateValidity(false);

                    if( isValid ) {
                        ProductWithTags result = new ProductWithTags();
                        result.product = resourceResourcePair.first.getData();
                        result.tags = resourceResourcePair.second.getData();
                        savable.setSavedResult(Resource.success(result));
                    }
                    else {
                        savable.setSavedResult(Resource.error(
                                new FormException(
                                        getApplication().getString(R.string.form_error_invalid)
                                ),
                                null
                        ));
                    }
                }
            });
        });

        savable.save();
    }

    @Override
    public MediatorLiveData<Boolean> onSave() {
        return savable.onSave();
    }

    @Override
    public MediatorLiveData<Resource<ProductWithTags>> onSaved() {
        return savable.onSaved();
    }
}
