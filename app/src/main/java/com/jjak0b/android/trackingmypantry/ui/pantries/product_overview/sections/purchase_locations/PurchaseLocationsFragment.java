package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.purchase_locations;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.ProductOverviewViewModel;

public class PurchaseLocationsFragment extends Fragment {

    private PurchaseLocationsViewModel mViewModel;
    private ProductOverviewViewModel mProductViewModel;

    public static PurchaseLocationsFragment newInstance() {
        return new PurchaseLocationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PurchaseLocationsViewModel.class);
        mProductViewModel = new ViewModelProvider(requireParentFragment()).get(ProductOverviewViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.purchase_locations_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}