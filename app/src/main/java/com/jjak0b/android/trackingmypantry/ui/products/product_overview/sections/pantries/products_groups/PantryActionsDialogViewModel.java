package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.repositories.PantriesRepository;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;

import java.util.Objects;

public class PantryActionsDialogViewModel extends AndroidViewModel {

    private MediatorLiveData<Resource<String>> mName;
    private PantriesRepository pantriesRepository;
    private LiveData<Resource<Pantry>> mSearchSource;
    private MutableLiveData<Resource<Pantry>> mPantry;

    public PantryActionsDialogViewModel(@NonNull Application application) {
        super(application);
        pantriesRepository = PantriesRepository.getInstance(application);
        mPantry = new MutableLiveData<>(Resource.loading(null));
        mName = new MediatorLiveData<>();
        mName.setValue(Resource.loading(null));
    }

    public void setPantry(@NonNull Resource<Pantry> pantry) {
        mPantry.setValue(pantry);
        if( pantry.getData() != null ) {
            setName(pantry.getData().getName());
        }
    }

    public LiveData<Resource<String>> getName() {
        return mName;
    }

    public void setName(String mName) {
        if( !Objects.equals(mName, this.mName.getValue().getData()) ) {
            this.mName.setValue(Resource.loading(mName));

            if( TextUtils.isEmpty(mName) ) {
                this.mName.postValue(Resource.error(
                        new FormException(getApplication().getString(R.string.field_error_empty)),
                        mName
                ));
                return;
            }

            LiveData<Resource<Pantry>> source = searchPantry(mName);

            // remove old search to avoid name replacement
            if( mSearchSource != null)
                this.mName.removeSource(mSearchSource);
            mSearchSource = source;

            this.mName.addSource( source, resource -> {
                if( resource.getStatus() != Status.LOADING) {
                    this.mName.removeSource(source);
                    if( resource.getData() != null) {
                        this.mName.setValue(Resource.error(
                                new FormException(getApplication().getString(R.string.pantry_name_already_exists)),
                                mName
                        ));
                    }
                    else {
                        this.mName.setValue(Resource.success(mName));
                    }
                }
            });
        }
    }

    private LiveData<Resource<Pantry>> searchPantry(String name) {
        return pantriesRepository.searchPantry(name);
    }

    public LiveData<Resource<Integer>> submitRename() {
        return Transformations.forwardOnce(mName, input -> {
            return Transformations.forwardOnce(mPantry, resource -> {
                Pantry pantry = resource.getData();
                pantry = pantry.clone();
                pantry.setName(input.getData());
                return pantriesRepository.update(pantry);
            });
        });
    }

    public LiveData<Resource<Void>> submitRemove() {
        return Transformations.forwardOnce(mPantry, resource -> {
            return pantriesRepository.remove(resource.getData());
        });
    }
}
