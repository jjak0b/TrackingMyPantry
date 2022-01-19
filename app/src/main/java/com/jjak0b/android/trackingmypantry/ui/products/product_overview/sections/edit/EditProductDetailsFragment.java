package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.edit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.register_product.SharedProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;

public class EditProductDetailsFragment extends ProductDetailsFragment {

    private static final String TAG = "EditProductInfoFragment";
    private SharedProductViewModel mProductViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProductViewModel = new ViewModelProvider(requireParentFragment()).get(SharedProductViewModel.class);
    }

    @Override
    protected ProductDetailsViewModel initViewModel() {
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

        final TextInputLayout barcodeInputLayout = view.findViewById(R.id.barcodeInputLayout);

        barcodeInputLayout.setEnabled(false);
        barcodeInputLayout.setStartIconVisible(false);
        barcodeInputLayout.setEndIconVisible(false);

        mProductViewModel.getProduct().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    getViewModel().setProduct(resource.getData());
                    break;
                default:
                    getViewModel().setProduct((Product) null);
                    break;
            }
        });

        setupSave(view, savedInstanceState);
    }


    public void setupSave(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final FloatingActionButton fabSave = view.findViewById(R.id.fab_save);

        fabSave.setOnClickListener( v -> getViewModel().save());

        getViewModel().canSave().observe(getViewLifecycleOwner(), canSave -> {
            fabSave.setEnabled(canSave);
        });

        getViewModel().onSave().observe( getViewLifecycleOwner(), isSaving -> {
            if( isSaving ){
                getViewModel().saveComplete();
            }
        });

        getViewModel().onSavedResult().observe(getViewLifecycleOwner(), resource -> {
            getViewModel().enableSave(resource.getStatus() != Status.LOADING );
            switch (resource.getStatus()) {
                case LOADING:

                    break;
                case SUCCESS:
                    Log.d(TAG, "Saved successfully: " + resource.getData() );
                    Navigation.findNavController(view)
                            .popBackStack();
                    break;
                case ERROR:
                    new AlertDialog.Builder(requireContext())
                            .setMessage(ErrorsUtils.getErrorMessage(
                                    requireContext(),
                                    resource.getError(),
                                    TAG)
                            )
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    break;
            }
        });
    }
}
