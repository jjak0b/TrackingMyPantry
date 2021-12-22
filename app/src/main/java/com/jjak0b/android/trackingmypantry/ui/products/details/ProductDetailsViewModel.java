package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.repositories.PantryRepository;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailsViewModel extends ProductInfoViewModel {

    protected PantryRepository pantryRepository;
    private MutableLiveData<List<ProductTag>> original;
    private MutableLiveData<List<ProductTag>> assignedTags;
    private LiveEvent<Resource<List<ProductTag>>> mTagsResult;

    public ProductDetailsViewModel(@NonNull Application application) {
        super(application);

        pantryRepository = PantryRepository.getInstance(application);

        assignedTags = (MutableLiveData<List<ProductTag>>) Transformations.map(
                original,
                (Function<List<ProductTag>, List<ProductTag>>) input -> {
                    // maybe should be deep copy
                    if( input != null )
                        return new ArrayList<>(input);
                    else
                        return new ArrayList<>(0);
                }
        );
    }

    @Override
    public void save() {
        super.save();
        // tags stuff

        ProductWithTags value = new ProductWithTags();
        mTagsResult.addSource(onSave(), aBoolean -> {
            if( aBoolean ){
                mTagsResult.setValue(Resource.loading(null));
                return;
            }
            mTagsResult.removeSource(onSave());

            mTagsResult.addSource(getAssignedTags(), productTags -> {
                mTagsResult.removeSource(getAssignedTags());
                mTagsResult.setValue(Resource.success(productTags));
            });
        });
    }

    public LiveData<Resource<ProductWithTags>> getResultProductWithTags() {
        return com.jjak0b.android.trackingmypantry.data.api.Transformations.forward(super.onSaved(), productResource -> {
            return com.jjak0b.android.trackingmypantry.data.api.Transformations.forward(mTagsResult, tagsResource -> {
                ProductWithTags productWithTags = new ProductWithTags();
                productWithTags.product = productResource.getData();
                productWithTags.tags = tagsResource.getData();
                return new MutableLiveData<>(Resource.success(productWithTags));
            });
        });
    }

    public void setProduct(ProductWithTags productWithTags) {
        if( productWithTags != null){
            super.setProduct(productWithTags.product);
            original.setValue(productWithTags.tags);
        }
        else {
            super.setProduct(null);
            original.setValue(null);
        }
    }

    public void setAssignedTags(List<ProductTag> tags ) {
        assignedTags.setValue( tags );
    }

    public LiveData<List<ProductTag>> getAssignedTags() {
        return assignedTags;
    }

    public LiveData<List<ProductTag>> getSuggestionTags() {
        return pantryRepository.getAllProductTags();
    }
}
