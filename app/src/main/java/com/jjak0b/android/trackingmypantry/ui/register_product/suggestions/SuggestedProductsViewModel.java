package com.jjak0b.android.trackingmypantry.ui.register_product.suggestions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hadilq.liveevent.LiveEvent;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.repositories.SuggestedProductsRepository;
import com.jjak0b.android.trackingmypantry.data.services.API.CreateProduct;
import com.jjak0b.android.trackingmypantry.data.services.API.VoteResponse;

import java.util.List;

public class SuggestedProductsViewModel extends AndroidViewModel {

    private SuggestedProductsRepository mSuggestionsRepository;
    private MutableLiveData<Resource<Product>> onProductVote;

    public SuggestedProductsViewModel(@NonNull Application application) {
        super(application);
        mSuggestionsRepository = SuggestedProductsRepository.getInstance(application);
        onProductVote = new MutableLiveData<>(Resource.loading(null));
    }

    public LiveData<Resource<List<Product>>> getProducts(String barcode) {
        return mSuggestionsRepository.getProducts(barcode);
    }

    public LiveData<Resource<VoteResponse>> vote(@NonNull Product product) {
        onProductVote.setValue(Resource.success(product));
        return mSuggestionsRepository.vote(product.getId(), 1);
    }

    public void newProduct() {
        onProductVote.setValue(Resource.success(null));
    }

    public LiveData<Resource<Product>> onProductVote() {
        return onProductVote;
    }
}
