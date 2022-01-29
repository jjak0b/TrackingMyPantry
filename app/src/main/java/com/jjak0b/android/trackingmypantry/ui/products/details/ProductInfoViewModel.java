package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.Application;
import android.graphics.Bitmap;
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
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;
import com.jjak0b.android.trackingmypantry.ui.util.ISavable;
import com.jjak0b.android.trackingmypantry.ui.util.ImageUtil;
import com.jjak0b.android.trackingmypantry.ui.util.Savable;

import java.util.Objects;

public class ProductInfoViewModel extends AndroidViewModel implements ISavable<UserProduct> {
    private final static int BITMAP_SIZE = 256;
    private final static int BITMAP_COMPRESSION_QUALITY = 50;
    protected AppExecutors appExecutors;

    private MediatorLiveData<Resource<String>> barcode;
    private MediatorLiveData<Resource<String>> name;
    private MediatorLiveData<Resource<String>> description;
    private MediatorLiveData<Resource<String>> image;

    private MutableLiveData<UserProduct> originalProduct;

    private Savable<UserProduct> savable;

    public ProductInfoViewModel(Application application) {
        super(application);
        this.appExecutors = AppExecutors.getInstance();
        this.savable = new Savable<>();

        this.originalProduct = new MutableLiveData<>(null);


        // set required fields as "loading" so, the starting validity fails
        // set optional fields as success because even on init state are still valid

        this.barcode = new MediatorLiveData<>();
        this.barcode.setValue(Resource.loading(null)); // required
        this.barcode.addSource(originalProduct, product -> {
            if( product != null ) {
                setBarcode(product.getBarcode());
            }
        });

        this.name = new MediatorLiveData<>();
        this.name.setValue(Resource.loading(null)); // required
        this.name.addSource(originalProduct, product -> {
            if( product != null ) {
                setName(product.getName());
            }
        });

        this.description = new MediatorLiveData<>();
        this.description.setValue(Resource.success(null)); // optional
        this.description.addSource(originalProduct, product -> {
            if( product != null ) {
                setDescription(product.getDescription());
            }
        });

        this.image = new MediatorLiveData<>();
        this.image.setValue(Resource.success(null)); // optional
        this.image.addSource(originalProduct, input -> {
            if( input != null ) {
                setImage(input.getImg());
            }
        });
    }

    @Override
    protected void onCleared() {
        savable.onCleared();
        originalProduct.setValue(null);
        originalProduct = null;
        super.onCleared();
    }

    public void enableSave(boolean enable ) {
        savable.enableSave(enable);
    }

    public boolean updateValidity() {
        boolean isValid = true;

        isValid = isValid && Transformations.onValid(getBarcode().getValue(), null);
        isValid = isValid && Transformations.onValid(getName().getValue(), null);
        isValid = isValid && Transformations.onValid(getDescription().getValue(), null);
        isValid = isValid && Transformations.onValid(getImage().getValue(), null);

        savable.enableSave(isValid);
        return isValid;
    }

    public LiveData<Boolean> canSave() {
        return savable.canSave();
    }

    public void saveComplete() {
        savable.saveComplete();
    }

    public void save() {
        LiveData<Boolean> onSave = this.onSave();
        MediatorLiveData<Resource<UserProduct>> onSaved = savable.onSaved();

        savable.save();

        onSaved.addSource(onSave, isSaving -> {
            if( isSaving ){
                savable.setSavedResult(Resource.loading(null));
                return;
            }
            onSaved.removeSource(onSave);

            onSaved.addSource(getProduct(), old -> {
                onSaved.removeSource(getProduct());

                UserProduct builder = new UserProduct(old);

                builder.setBarcode(getBarcode().getValue().getData());
                builder.setName(getName().getValue().getData());
                builder.setDescription(getDescription().getValue().getData());

                LiveData<Resource<String>> resourceURI = Transformations.forward(getImage(), resourceImageURI -> {
                   // forward result if not changed
                   if( Objects.equals(old.getImg(), resourceImageURI.getData() ) ) {
                       return androidx.lifecycle.Transformations.map(getImage(), input -> input );
                   }
                   // scale current bitmap and encode as URI
                   else {
                       return Transformations.forward(ImageUtil.getBitmap(resourceImageURI.getData()), resourceBitmap -> {
                           return Transformations.forward(
                               Transformations.simulateApi(
                                       appExecutors.diskIO(),
                                       appExecutors.mainThread(),
                                       () -> Bitmap.createScaledBitmap(resourceBitmap.getData(), BITMAP_SIZE, BITMAP_SIZE, true)
                               ),
                               resourceScaled -> ImageUtil.getURI(resourceScaled.getData(), BITMAP_COMPRESSION_QUALITY )
                           );
                       });
                   }
                });

                onSaved.addSource(resourceURI, resource -> {
                    if (resource.getStatus() != Status.LOADING) {
                        onSaved.removeSource(resourceURI);
                        builder.setImg(resource.getData());
                        savable.setSavedResult(Resource.success(builder));
                    }
                });
            });
        });
    }

    public MediatorLiveData<Boolean> onSave() {
        return savable.onSave();
    }

    public MediatorLiveData<Resource<UserProduct>> onSaved() {
        return savable.onSaved();
    }

    public LiveData<UserProduct> getProduct() {
        return originalProduct;
    }

    public void setProduct(UserProduct product) {
        originalProduct.setValue(product);
    }

    public LiveData<Resource<String>> getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        if(!Objects.equals(barcode, this.barcode.getValue().getData() )) {
            this.barcode.setValue(Resource.loading(barcode));
            String checkBarcode = barcode != null ? barcode.trim() : null;
            Throwable error = null;

            if(TextUtils.isEmpty(checkBarcode)) {
                error = new FormException(getApplication().getString(R.string.field_error_empty));
            }
            if( error != null ) {
                this.barcode.setValue(Resource.error(error, barcode ));
            }
            else {
                this.barcode.setValue(Resource.success(barcode));
            }
            updateValidity();
        }
    }


    public LiveData<Resource<String>> getName() {
        return name;
    }

    public void setName(String name) {
        if(!Objects.equals(name, this.name.getValue().getData() )) {
            this.name.setValue(Resource.loading(name));
            String checkName = name != null ? name.trim() : null;
            Throwable error = null;

            if(TextUtils.isEmpty(checkName)) {
                error = new FormException(getApplication().getString(R.string.field_error_empty));
            }

            if( error != null ) {
                this.name.setValue(Resource.error(error, name ));
            }
            else {
                this.name.setValue(Resource.success(name));
            }
            updateValidity();
        }
    }

    public LiveData<Resource<String>> getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(!Objects.equals(description, this.description.getValue().getData() )) {
            this.description.setValue(Resource.loading(description));
            // String checkDescription = description != null ? description.trim() : null;
            this.description.setValue(Resource.success(description));
            updateValidity();
        }
    }

    public LiveData<Resource<String>> getImage() {
        return image;
    }

    public void setImage(String imageUri ){
        if( !Objects.equals(this.image.getValue().getData(), imageUri) ) {
            this.image.setValue(Resource.success(imageUri));
            updateValidity();
        }
    }
    public void setImage(Bitmap image) {
        LiveData<Resource<String>> mURI = ImageUtil.getURI(image);
        this.image.addSource(mURI, resource -> {
            if( resource.getStatus() == Status.LOADING ) {
                this.image.setValue(resource);
            }
            else {
                this.image.removeSource(mURI);
                this.image.setValue(resource);
                updateValidity();
            }
        });
    }
}