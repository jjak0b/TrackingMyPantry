package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInstanceDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInstanceDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.register_product._RegisterProductViewModel;

public class SectionProductInstanceDetailsFragment extends ProductInstanceDetailsFragment {

    private final static String TAG = "SectionProductInstanceDetailsFragment";
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

        setupSave(view, savedInstanceState);
        setupReset(view, savedInstanceState);
    }

    @Override
    public void setupSave(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mSharedViewModel.onSaveInfoDetails().observe(getViewLifecycleOwner(), isSaving -> {
            if( !isSaving ) return;
            Log.d(TAG, "force saving");
            getViewModel().save();
        });

        super.setupSave(view, savedInstanceState);
        getViewModel().onSave().observe( getViewLifecycleOwner(), shouldSave -> {
            Log.d(TAG, "isSaving=" +shouldSave);
            if( !shouldSave ) return;

            getViewModel().saveComplete();
        });

        getViewModel().onSaved().observe(getViewLifecycleOwner(), resource -> {
            Log.d(TAG, "Saved Product group details: " + resource );
            mSharedViewModel.setInfoDetails(resource);
        });
    }

    @Override
    public void setupReset(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mSharedViewModel.onReset().observe(getViewLifecycleOwner(), shouldReset -> {
            getViewModel().reset();
        });
    }
}
