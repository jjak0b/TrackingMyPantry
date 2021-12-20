package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.edit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInfoFragment;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInfoViewModel;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.ProductOverviewViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;

public class EditProductInfoFragment extends ProductInfoFragment {

    private ProductOverviewViewModel mProductViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProductViewModel = new ViewModelProvider(requireParentFragment()).get(ProductOverviewViewModel.class);
    }

    @Override
    protected ProductInfoViewModel initViewModel() {
        return new ViewModelProvider(this).get(EditProductDetailsViewModel.class);
    }

    private EditProductDetailsViewModel getViewModel() {
        return (EditProductDetailsViewModel) mViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_product_details_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final FloatingActionButton fabSave = view.findViewById(R.id.fab_save);
        final TextInputLayout barcodeInputLayout = view.findViewById(R.id.barcodeInputLayout);

        barcodeInputLayout.setEnabled(false);
        barcodeInputLayout.setStartIconVisible(false);
        barcodeInputLayout.setEndIconVisible(false);

        mProductViewModel.getProduct().observe(getViewLifecycleOwner(), getViewModel()::setProduct );

        fabSave.setOnClickListener( v -> getViewModel().save());

        getViewModel().onSave().observe( getViewLifecycleOwner(), shouldSave -> {
            if( shouldSave ){
                getViewModel().saveComplete();
                return;
            }
        });

        getViewModel().onSaved().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() != Status.LOADING) {
                if (resource.getData() != null) {
                    Futures.addCallback(
                            getViewModel().submit(),
                            new FutureCallback<Void>() {
                                @Override
                                public void onSuccess(@Nullable Void result) {
                                    Navigation.findNavController(view)
                                            .popBackStack();
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    Log.e("Edit product", "Failed save", t);
                                    String errorMSg = ErrorsUtils.getErrorMessage(
                                            requireContext(), resource.getError(), "EditProductFragment"
                                    );
                                    if( errorMSg != null ) {
                                        new AlertDialog.Builder(requireContext())
                                                .setMessage(errorMSg)
                                                .show();
                                    }
                                }
                            },
                            ContextCompat.getMainExecutor(requireContext())
                    );
                }
                else if( resource.getStatus() == Status.ERROR ){
                    String errorMSg = ErrorsUtils.getErrorMessage(
                            requireContext(), resource.getError(), "EditProductFragment"
                    );
                    if( errorMSg != null ) {
                        new AlertDialog.Builder(requireContext())
                                .setMessage(errorMSg)
                                .show();
                    }
                }
            }
        });
    }
}
