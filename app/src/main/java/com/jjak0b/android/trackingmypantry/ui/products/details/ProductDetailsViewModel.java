package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.repositories.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.ui.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductDetailsViewModel extends AndroidViewModel {

    protected PantryRepository pantryRepository;
    private MutableLiveData<ProductWithTags> originalProduct;
    private MutableLiveData<String> barcode;
    private MutableLiveData<String> name;
    private MutableLiveData<String> description;
    private MutableLiveData<List<ProductTag>> assignedTags;
    private MutableLiveData<Bitmap> image;
    private LiveEvent<Boolean> onSave;

    public ProductDetailsViewModel(Application application) {
        super(application);

        pantryRepository = PantryRepository.getInstance(application);
        originalProduct = new MutableLiveData<>();

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

        onSave = new LiveEvent<>();
    }

    @Override
    protected void onCleared() {
        originalProduct.setValue(null);
        originalProduct = null;
        super.onCleared();
    }

    public void save() {
        onSave.setValue(true);
        onSave.postValue(false);
    }

    public LiveData<Boolean> onSave() {
        return onSave;
    }

    public LiveData<ProductWithTags> getProduct() {
        return originalProduct;
    }

    public void setProduct(ProductWithTags productWithTags) {
        originalProduct.setValue(productWithTags);
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

}