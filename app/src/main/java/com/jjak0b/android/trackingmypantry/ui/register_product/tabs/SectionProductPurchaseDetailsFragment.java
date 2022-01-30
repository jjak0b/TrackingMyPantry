package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.ui.products.details.ProductPurchaseDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductPurchaseDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

public class SectionProductPurchaseDetailsFragment extends ProductPurchaseDetailsFragment {

    private final static String TAG = "SectionProductPurchaseDetailsFragment";
    private RegisterProductViewModel mSharedViewModel;

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
        mSharedViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSave(view, savedInstanceState);
        setupReset(view, savedInstanceState);
    }

    @Override
    public void setupSave(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mSharedViewModel.onSavePurchaseDetails().observe(getViewLifecycleOwner(), isSaving -> {
            if( !isSaving  ) return;
            Log.d(TAG, "force saving");

            // close any open keyboard
            InputUtil.hideKeyboard(requireActivity());

            // getViewModel().save();
        });

        getViewModel().canSave().observe(getViewLifecycleOwner(), canSave -> {
            Log.d(TAG, "trigger autosave");
            getViewModel().save();
        });

        getViewModel().onSave().observe( getViewLifecycleOwner(), isSaving -> {
            Log.d(TAG, "isSaving=" +isSaving);
            if( !isSaving ) return;

            getViewModel().saveComplete();
        });

        getViewModel().onSaved().observe(getViewLifecycleOwner(), resource -> {
            Log.d(TAG, "Saved purchase details: " + resource );
            mSharedViewModel.setPurchaseDetails(resource);
        });
    }

    @Override
    public void setupReset(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mSharedViewModel.onReset().observe(getViewLifecycleOwner(), shouldReset -> {
            getViewModel().reset();
        });
    }
}
