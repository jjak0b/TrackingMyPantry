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
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;
import com.jjak0b.android.trackingmypantry.ui.util.ISavable;
import com.jjak0b.android.trackingmypantry.ui.util.ImageUtil;
import com.jjak0b.android.trackingmypantry.ui.util.Savable;

import java.util.Objects;

public class ProductInfoViewModel extends AndroidViewModel implements ISavable<Product> {
    private final static int BITMAP_SIZE = 256;
    protected AppExecutors appExecutors;

    private MediatorLiveData<Resource<String>> barcode;
    private MediatorLiveData<Resource<String>> name;
    private MediatorLiveData<Resource<String>> description;
    private MediatorLiveData<Resource<Bitmap>> image;

    private MutableLiveData<Product> originalProduct;

    private Savable<Product> savable;

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
            if( input != null && input.getImg() != null ) {
                LiveData<Resource<Bitmap>> mBitmap = ImageUtil.getBitmap(input.getImg());
                image.addSource(mBitmap, bitmapResource -> {
                    switch (bitmapResource.getStatus()) {
                        case ERROR:
                            setImage(null);
                            image.removeSource(mBitmap);
                            break;
                        case SUCCESS:
                            image.removeSource(mBitmap);
                            setImage(bitmapResource.getData());
                            break;
                    }
                });
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

    private boolean updateValidity() {
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
        savable.save();

        savable.onSaved().removeSource(savable.onSave());
        savable.onSaved().addSource(savable.onSave(), aBoolean -> {
            if( aBoolean ){
                savable.setSavedResult(Resource.loading(null));
                return;
            }
            savable.onSaved().removeSource(savable.onSave());

            savable.onSaved().addSource(getProduct(), old -> {
                savable.onSaved().removeSource(getProduct());

                Product.Builder builder = new Product.Builder().from(old);

                builder.setBarcode(getBarcode().getValue().getData());
                builder.setName(getName().getValue().getData());
                builder.setDescription(getDescription().getValue().getData());

                Bitmap bitmap = getImage().getValue().getData();
                // scale current bitmap and encode as URI
                LiveData<Resource<String>> resourceURI = bitmap != null ? Transformations.forward(
                        Transformations.simulateApi(
                                appExecutors.diskIO(),
                                appExecutors.mainThread(),
                                () -> Bitmap.createScaledBitmap(bitmap, BITMAP_SIZE, BITMAP_SIZE, true)
                        ),
                        resourceScaled -> ImageUtil.getURI(resourceScaled.getData())
                ) : new MutableLiveData<>(Resource.success(null));

                savable.onSaved().addSource(resourceURI, resource -> {
                    if (resource.getStatus() != Status.LOADING) {
                        savable.onSaved().removeSource(resourceURI);
                        builder.setImg(resource.getData());
                        savable.setSavedResult(Resource.success(builder.build()));
                    }
                });
            });
        });
    }

    public MediatorLiveData<Boolean> onSave() {
        return savable.onSave();
    }

    public MediatorLiveData<Resource<Product>> onSaved() {
        return savable.onSaved();
    }

    public LiveData<Product> getProduct() {
        return originalProduct;
    }

    public void setProduct(Product product) {
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

    public LiveData<Resource<Bitmap>> getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        if( !Objects.equals(this.image.getValue().getData(), image) ) {
            this.image.setValue(Resource.success(image));
            updateValidity();
        }
    }
}