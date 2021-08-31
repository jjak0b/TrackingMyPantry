package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.edit;

import android.app.Application;
import android.graphics.Bitmap;
import android.view.animation.Transformation;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.ui.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditProductDetailsViewModel extends AndroidViewModel {

    private PantryRepository pantryRepository;
    private MutableLiveData<ProductWithTags> originalProduct;
    private MutableLiveData<Product.Builder> productBuilder;
    private MutableLiveData<String> barcode;
    private MutableLiveData<String> name;
    private MutableLiveData<String> description;
    private MutableLiveData<List<ProductTag>> assignedTags;
    private MutableLiveData<Bitmap> image;

    public EditProductDetailsViewModel(Application application) {
        super(application);

        pantryRepository = PantryRepository.getInstance(application);
        originalProduct = new MutableLiveData<>();

        productBuilder = (MutableLiveData<Product.Builder>) Transformations.map(originalProduct,
                input -> input != null ?  new Product.Builder().from(input.product) : null
        );

        barcode = (MutableLiveData<String>) Transformations.map(originalProduct,
                input -> input != null ? input.product.getBarcode() : null
        );

        name = (MutableLiveData<String>) Transformations.map(originalProduct,
                input -> input != null ? input.product.getName() : null
        );

        description = (MutableLiveData<String>) Transformations.map(originalProduct,
                input -> input != null ? input.product.getDescription() : null
        );

        image = (MutableLiveData<Bitmap>) Transformations.map(
                originalProduct,
                input -> {
                    if( input != null && input.product.getImg() != null ) {
                        try {
                            return ImageUtil.convert(input.product.getImg());
                        } catch (IllegalArgumentException exception) {
                            return null;
                        }
                    }
                    return null;
                }
        );

        assignedTags = (MutableLiveData<List<ProductTag>>) Transformations.map(
                originalProduct,
                (Function<ProductWithTags, List<ProductTag>>) input -> {
                    // maybe should be deep copy
                    if( input != null )
                        return new ArrayList<>(input.tags);
                    else
                        return new ArrayList<>(0);
                }
        );
    }

    @Override
    protected void onCleared() {
        productBuilder.setValue(null);
        productBuilder = null;
        super.onCleared();
    }

    public void setProduct(ProductWithTags productWithTags) {
        originalProduct.setValue(productWithTags);
        Product.Builder productBuilder = null;

        if( productWithTags != null )
            productBuilder = new Product.Builder()
                    .from(productWithTags.product );
        this.productBuilder.setValue( productBuilder );
    }

    public void setAssignedTags( List<ProductTag> tags ) {
        assignedTags.setValue( tags );
    }

    public LiveData<List<ProductTag>> getAssignedTags() {
        return assignedTags;
    }

    public LiveData<List<ProductTag>> getSuggestionTags() {
        return pantryRepository.getAllProductTags();
    }

    public LiveData<Bitmap> getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        if( !Objects.equals(this.image.getValue(), image) )
            this.image.setValue(image);
    }

    public LiveData<String> getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode){
        if( !Objects.equals(this.barcode.getValue(), barcode) )
            this.barcode.setValue(barcode);
    }

    public LiveData<String> getName() {
        return name;
    }

    public void setName(String name) {
        if( !Objects.equals(this.name.getValue(), name) )
            this.name.setValue(name);
    }

    public LiveData<String> getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if( !Objects.equals(this.description.getValue(), description) )
            this.description.setValue(description);
    }

    public ListenableFuture<ProductWithTags> save() {

        ProductWithTags productWithTags = new ProductWithTags();
        productWithTags.product = productBuilder.getValue()
                .setBarcode(getBarcode().getValue())
                .setName(getName().getValue())
                .setDescription(getDescription().getValue())
                .setImg(getImage().getValue() != null ? ImageUtil.convert(getImage().getValue()) : null )
                .build();
        productWithTags.tags = getAssignedTags().getValue();
        return pantryRepository.updateProduct( productWithTags );
    }

}