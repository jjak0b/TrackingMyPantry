package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInstanceDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInstanceDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.register_product._RegisterProductViewModel;

public class SectionProductInstanceDetailsFragment extends ProductInstanceDetailsFragment {

    private _RegisterProductViewModel mSharedViewModel;

    @Override
    @NonNull
    public ProductInstanceDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(SectionProductInstanceDetailsViewModel.class);
    }

    @NonNull
    private SectionProductInstanceDetailsViewModel getViewModel() {
        return (SectionProductInstanceDetailsViewModel) mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedViewModel = new ViewModelProvider(requireActivity()).get(_RegisterProductViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSharedViewModel.onSaveInfoDetails().observe(getViewLifecycleOwner(), isSaving -> {
            if( !isSaving ) return;
            getViewModel().save();
        });

        getViewModel().onSave().observe( getViewLifecycleOwner(), shouldSave -> {
            if( !shouldSave ) return;

            getViewModel().saveComplete();
        });

        getViewModel().onSaved().observe(getViewLifecycleOwner(), resource -> {
            mSharedViewModel.setInfoDetails(resource);
        });
    }
}
