package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInstanceDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInstanceDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;

public class SectionProductInstanceDetailsFragment extends ProductInstanceDetailsFragment {

    private RegisterProductViewModel mSharedViewModel;

    @Override
    protected ProductInstanceDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(SectionProductInstanceDetailsViewModel.class);
    }

    @NonNull
    private SectionProductInstanceDetailsViewModel getViewModel() {
        return (SectionProductInstanceDetailsViewModel) mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSharedViewModel.getProductInstance().observe( getViewLifecycleOwner(), productInstance -> {
            if( productInstance == null ){
                // set default values in fields
                mSharedViewModel.resetProductInstance();
                return;
            }
        });
    }
}
