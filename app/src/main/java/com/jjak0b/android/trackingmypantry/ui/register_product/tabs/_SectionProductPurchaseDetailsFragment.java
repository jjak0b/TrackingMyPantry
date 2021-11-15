package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.ui.products.details.ProductPurchaseDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductPurchaseDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;

public class _SectionProductPurchaseDetailsFragment extends ProductPurchaseDetailsFragment {

    private RegisterProductViewModel mSharedViewModel;

    @Override
    protected ProductPurchaseDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(_SectionProductPurchaseDetailsViewModel.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSharedViewModel.getProductPurchaseInfo().observe( getViewLifecycleOwner(), purchaseInfo -> {
            if( purchaseInfo == null ){
                // set default values in fields
                mSharedViewModel.resetPurchaseInfo();
                return;
            }
        });
    }
}
