package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.ui.products.details.ProductPurchaseDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductPurchaseDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.register_product._RegisterProductViewModel;

public class SectionProductPurchaseDetailsFragment extends ProductPurchaseDetailsFragment {

    private _RegisterProductViewModel mSharedViewModel;

    @Override
    public ProductPurchaseDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(SectionProductPurchaseDetailsViewModel.class);
    }

    @NonNull
    private SectionProductPurchaseDetailsViewModel getViewModel() {
        return (SectionProductPurchaseDetailsViewModel) mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedViewModel = new ViewModelProvider(requireActivity()).get(_RegisterProductViewModel.class);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSharedViewModel.onSavePurchaseDetails().observe(getViewLifecycleOwner(), shouldSave -> {
            if( !shouldSave  ) return;
            getViewModel().save();
        });

        getViewModel().onSave().observe( getViewLifecycleOwner(), isSaving -> {
            if( !isSaving ) return;

            getViewModel().saveComplete();
        });

        getViewModel().onSaved().observe(getViewLifecycleOwner(), purchaseInfoWithPlaceResource -> {
            mSharedViewModel.setPurchaseDetails(purchaseInfoWithPlaceResource);
        });
    }
}
