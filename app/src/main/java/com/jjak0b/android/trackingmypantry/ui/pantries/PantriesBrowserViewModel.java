package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;

import java.util.List;

public class PantriesBrowserViewModel extends AndroidViewModel {

    private PantryRepository pantryRepository;
    private MutableLiveData<String> productID;
    private LiveData<List<Pantry>> list;

    public PantriesBrowserViewModel(@NonNull Application application) {
        super(application);
        pantryRepository = PantryRepository.getInstance(application);
        productID = new MutableLiveData<>();
        list = Transformations.switchMap(
                productID,
                new Function<String, LiveData<List<Pantry>>>() {
                    @Override
                    public LiveData<List<Pantry>> apply(String id) {
                        return pantryRepository.getPantriesWithProductInstanceGroupsOf(id);
                    }
                });
    }

    public void setProductID( String id ){
        productID.setValue( id );
    }

    LiveData<List<Pantry>> getList(){
        return list;
    }
}