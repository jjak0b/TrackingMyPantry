package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInstanceDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInstanceDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

public class SectionProductInstanceDetailsFragment extends ProductInstanceDetailsFragment {

    private final static String TAG = "SectionProductInstanceDetailsFragment";
    private RegisterProductViewModel mSharedViewModel;

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
        mSharedViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSave(view, savedInstanceState);
        setupReset(view, savedInstanceState);

        mSharedViewModel.getProductGroupDetails().observe(getViewLifecycleOwner(), resource -> {
            if( resource.getStatus() != Status.LOADING ) {
                getViewModel().setDetails(resource.getData());
            }
        });
    }

    @Override
    public void setupSave(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mSharedViewModel.onSaveInfoDetails().observe(getViewLifecycleOwner(), isSaving -> {
            if( !isSaving ) return;
            Log.d(TAG, "force saving");

            // close any open keyboard
            InputUtil.hideKeyboard(requireActivity());

            // getViewModel().save();
        });

        getViewModel().canSave().observe(getViewLifecycleOwner(), canSave -> {
            Log.d(TAG, "trigger autosave");
            getViewModel().save();
        });

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
