package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.IOBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;
import com.jjak0b.android.trackingmypantry.ui.util.Savable;
import com.jjak0b.android.trackingmypantry.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductDetailsViewModel extends ProductInfoViewModel {

    protected ProductsRepository productsRepository;

    private LiveData<Resource<List<ProductTag>>> mSuggestionsTags;
    private MediatorLiveData<Resource<List<ProductTag>>> mAssignedTags;
    private LiveData<Resource<List<ProductTag>>> mProductTagsSource;
    private MutableLiveData<Resource<List<ProductTag>>> mProductTagsDefaultSource;

    private Savable<ProductWithTags> savable;

    public ProductDetailsViewModel(@NonNull Application application) {
        super(application);

        productsRepository = ProductsRepository.getInstance(application);

        mSuggestionsTags = productsRepository.getTags();
        mAssignedTags = new MediatorLiveData<>();
        mProductTagsDefaultSource = new MutableLiveData<>(Resource.success(new ArrayList<>(0)));
        mAssignedTags.setValue(mProductTagsDefaultSource.getValue());


        savable = new Savable<>();

        // override assigned tags on barcode change, with the ones assigned to existing product
        final MutableLiveData<List<ProductTag>> mTags  = new MutableLiveData<>(new ArrayList<>(0));
        LiveData<Resource<List<ProductTag>>> mAssignedTagsOnBarcode =
                com.jjak0b.android.trackingmypantry.data.api.Transformations.forward(getBarcode(), resource -> {
                    String barcode = resource.getData();
                    return Transformations.forwardOnce(productsRepository.getDetails(barcode), detailsResource -> {
                        ProductWithTags details = detailsResource.getData();
                        if( details != null ) {
                            mTags.setValue(details.tags);
                            return IOBoundResource.adapt(appExecutors, mTags );
                        }
                        else {
                            return mProductTagsDefaultSource;
                        }
                    });
                });

        setProductTagsSource(mAssignedTagsOnBarcode);
    }


    private void setProductTagsSource(LiveData<Resource<List<ProductTag>>> source ) {
        if( mProductTagsSource != null ) {
            mAssignedTags.removeSource(mProductTagsSource);
        }
        if( source == null ) source = mProductTagsDefaultSource;

        mProductTagsSource = source;

        mAssignedTags.addSource(source, resource -> {
            mAssignedTags.setValue(resource);
        });
    }

    public void enableSave(boolean enable ) {
        super.enableSave(enable);
        savable.enableSave(enable);
    }

    public void save() {
        savable.save();
        super.save();

        LiveData<Boolean> onSave = this.onSave();
        MediatorLiveData<Resource<ProductWithTags>> onSaved = savable.onSaved();
        onSaved.addSource(onSave, isSaving -> {
            if( isSaving ) {
                savable.setSavedResult(Resource.loading(null));
                return;
            }
            onSaved.removeSource(onSave);

            LiveData<Resource<UserProduct>> mProduct = super.onSaved();

            onSaved.addSource(mProduct, resource -> {
                if (resource.getStatus() != Status.LOADING ) {
                    onSaved.removeSource(mProduct);

                    boolean isValid = resource.getStatus() == Status.SUCCESS;
                    if (isValid) {
                        ProductWithTags result = new ProductWithTags();
                        result.product = resource.getData();
                        result.tags = getAssignedTags().getValue().getData();
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
    }

    @Override
    public void saveComplete() {
        super.saveComplete();
        savable.saveComplete();
    }

    @Override
    public LiveData<Boolean> canSave() {
        final MediatorLiveData<Boolean> mediator = new MediatorLiveData<>();
        final ResourceUtils.PairLiveData<Boolean, Boolean> mPair = new ResourceUtils.PairLiveData<>(false, false );

        mPair.addSources(
                super.canSave(),
                savable.canSave(),
                value -> true,
                value -> true
        );

        mediator.addSource(mPair, booleansPair -> {
            mediator.setValue(booleansPair.first && booleansPair.second);
        });

        return mediator;
    }


    @Override
    public MediatorLiveData<Boolean> onSave() {
        final MediatorLiveData<Boolean> mediator = new MediatorLiveData<>();
        final ResourceUtils.PairLiveData<Boolean, Boolean> mPair = new ResourceUtils.PairLiveData<>(false, false );

        mPair.addSources(
                super.onSave(),
                savable.onSave(),
                value -> true,
                value -> true
        );

        mediator.addSource(mPair, booleansPair -> {
            mediator.setValue(booleansPair.first || booleansPair.second);
        });

        return mediator;
    }

    public MediatorLiveData<Resource<ProductWithTags>> onSavedResult() {
        return savable.onSaved();
    }

    @Override
    public boolean updateValidity() {
        boolean isValid = super.updateValidity() && Transformations.onValid(getAssignedTags().getValue(), null );
        savable.enableSave(isValid);
        return isValid;
    }

    public void setProduct(ProductWithTags productWithTags) {
        if( productWithTags != null){
            super.setProduct(productWithTags.product);
            setAssignedTags(productWithTags.tags);
        }
        else {
            super.setProduct(null);
            setAssignedTags(productWithTags.tags);
        }
    }

    public void setAssignedTags( List<ProductTag> tags ) {
        if(!Objects.equals(tags, mAssignedTags.getValue().getData())) {
            mAssignedTags.setValue(Resource.loading(tags));
            mAssignedTags.setValue(Resource.success(tags));
            updateValidity();
        }
    }

    public LiveData<Resource<List<ProductTag>>> getAssignedTags() {
        return mAssignedTags;
    }

    public LiveData<Resource<List<ProductTag>>> getSuggestionTags() {
        return mSuggestionsTags;
    }
}
