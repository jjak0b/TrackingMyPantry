package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.jjak0b.android.trackingmypantry.data.repositories.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.db.relationships.PantryWithProductInstanceGroups;

import java.util.List;
import java.util.Objects;

public class PantriesBrowserViewModel extends AndroidViewModel {

    private PantryRepository pantryRepository;
    private MutableLiveData<String> productID;
    private LiveData<List<PantryWithProductInstanceGroups>> list;
    private MutableLiveData<PantryWithProductInstanceGroups> mCurrentPantry;

    public PantriesBrowserViewModel(@NonNull Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        productID = new MutableLiveData<>();
        list = Transformations.switchMap(
                productID,
                id -> pantryRepository.getPantriesWithProductInstanceGroupsOf(id));
        mCurrentPantry = (MutableLiveData<PantryWithProductInstanceGroups>) Transformations.map( list, input -> {
            if (mCurrentPantry != null && mCurrentPantry.getValue() != null) {
                for (PantryWithProductInstanceGroups pantryWGroups : input) {
                    if (Objects.equals(pantryWGroups.pantry, mCurrentPantry.getValue().pantry))
                        return pantryWGroups;
                }
            }
            return null;
        });
    }

    public LiveData<PantryWithProductInstanceGroups> getCurrentPantry() {
        return mCurrentPantry;
    }

    public void setCurrentPantry(PantryWithProductInstanceGroups mCurrentPantry) {
        this.mCurrentPantry.postValue(mCurrentPantry);
    }

    public void setProductID(String id ){
        productID.setValue( id );
    }

    LiveData<List<PantryWithProductInstanceGroups>> getList(){
        return list;
    }

}