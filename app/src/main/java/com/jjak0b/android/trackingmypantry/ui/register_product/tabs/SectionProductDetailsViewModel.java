package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.app.Application;
import android.text.TextUtils;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.repositories.ProductsRepository;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SectionProductDetailsViewModel extends AndroidViewModel {

    private MutableLiveData<Resource<String>> mBarcode;
    private MutableLiveData<Resource<Product>> mProduct;
    private MutableLiveData<Resource<List<ProductTag>>> mAssignedTags;
    private ProductsRepository productsRepository;

    public SectionProductDetailsViewModel(Application application) {
        super(application);
        productsRepository = ProductsRepository.getInstance(application);

        mBarcode = new MutableLiveData<>(Resource.loading(null));
        mProduct = new MutableLiveData<>(Resource.loading(null));
        mAssignedTags = new MutableLiveData<>(Resource.success(new ArrayList<>(0)));
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
            getProductPreview();
        }
    }

    public LiveData<Resource<Product>> getProduct() {
        return mProduct;
    }

    public void setProduct(Product product) {
        if(!Objects.equals(product, mProduct.getValue().getData())) {
            Throwable error = null;
            if( product == null ) {
                mProduct.setValue(Resource.loading(null));
            }
            else {
                mProduct.setValue(Resource.success(product));
            }
        }
    }

    public void setProduct(ProductWithTags productWithTags) {
        if( productWithTags != null){
            setProduct(productWithTags.product);
            setAssignedTags(productWithTags.tags);
        }
        else {
            setProduct((Product) null);
            setAssignedTags(new ArrayList<>(0));
        }
    }

    public LiveData<Resource<List<ProductTag>>> getAssignedTags() {
        return mAssignedTags;
    }

    public void setAssignedTags(List<ProductTag> tags ) {
        mAssignedTags.setValue( Resource.loading(tags) );
        mAssignedTags.setValue( Resource.success(tags) );
    }

    public LiveData<Resource<List<ProductTag>>> getSuggestionTags() {
        return productsRepository.getTags();
    }

    void resetProduct() {
        setProduct((ProductWithTags) null);
    }

    public LiveData<Resource<ProductWithTags>> getProductPreview() {
        return Transformations.forward(getBarcode(), barcodeResource -> {
            return Transformations.forward(productsRepository.getDetails(barcodeResource.getData()), detailsResource -> {
                ProductWithTags model = detailsResource.getData();
                setProduct(model);
                return new MutableLiveData<>(detailsResource);
            });
        });
    }
}
