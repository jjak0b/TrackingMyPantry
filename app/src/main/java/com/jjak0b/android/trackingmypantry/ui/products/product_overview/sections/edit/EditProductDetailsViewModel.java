package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.edit;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductDetailsViewModel;

public class EditProductDetailsViewModel extends ProductDetailsViewModel {

    public EditProductDetailsViewModel(Application application) {
        super(application);
    }

    @Override
    public MediatorLiveData<Resource<ProductWithTags>> onSavedResult() {
        final MediatorLiveData<Resource<ProductWithTags>> mediator = new MediatorLiveData<>();
        final LiveData<Resource<ProductWithTags>> source = Transformations.forward(super.onSavedResult(), resource -> {
           return productsRepository.addDetails(resource.getData());
        });
        mediator.addSource(source, mediator::setValue );
        return mediator;
    }

}
