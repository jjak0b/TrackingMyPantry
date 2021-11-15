package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.BarcodeScannerActivity;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductDetailsFragment;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductFragmentDirections;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.register_product.SuggestedProductsViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;
import com.jjak0b.android.trackingmypantry.ui.util.Permissions;

public class SectionProductDetailsFragment extends ProductDetailsFragment {

    private RegisterProductViewModel mSharedViewModel;
    private SuggestedProductsViewModel mSuggestedProductsViewModel;

    static final String TAG = "RegisterProductFragment";
    private ActivityResultLauncher<Intent> scanLauncher;
    private ActivityResultLauncher<String[]> requestScanCameraPermissionsLauncher;

    @Override
    protected ProductDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(SectionProductDetailsViewModel.class);
    }

    @NonNull
    private SectionProductDetailsViewModel getViewModel() {
        return (SectionProductDetailsViewModel) mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
        mSuggestedProductsViewModel = new ViewModelProvider(requireParentFragment()).get(SuggestedProductsViewModel.class);

        requestScanCameraPermissionsLauncher = registerForActivityResult( new ActivityResultContracts.RequestMultiplePermissions(), areGranted -> {
            if( !areGranted.containsValue(false) ) {
                startScanner();
            }
        });

        scanLauncher = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), result -> {
            if( result.getResultCode() == Activity.RESULT_OK ) {
                if( result.getData() != null ){
                    String barcode = result.getData().getStringExtra(BarcodeScannerActivity.BARCODE);
                    mViewModel.setBarcode( barcode );
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_section_product_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.e( TAG, "onViewCreated");
        Log.e( TAG, getViewLifecycleOwner().toString() );

        final TextInputLayout barcodeInputLayout = view.findViewById(R.id.barcodeInputLayout);
        final EditText editBarcode = (EditText) view.findViewById(R.id.editTextBarcode);
        final View productForm = view.findViewById(R.id.productForm);

        boolean hasFeatureCamera = getContext().getPackageManager()
                .hasSystemFeature( PackageManager.FEATURE_CAMERA_ANY );

        barcodeInputLayout.setEnabled(true);
        barcodeInputLayout.setStartIconVisible(true);
        barcodeInputLayout.setEndIconVisible(hasFeatureCamera);

        editBarcode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(editBarcode.getText().toString());
                return true;
            }
            return false;
        });

        barcodeInputLayout.setStartIconOnClickListener(v -> {
            search( editBarcode.getText().toString() );
        });

        if(hasFeatureCamera){
            barcodeInputLayout.setEndIconOnClickListener( v -> {
                boolean hasPermissions = new Permissions.FeatureRequestBuilder()
                        .setRationaleMessage(R.string.menu_register_product_action_scan)
                        .setOnPositive(requestScanCameraPermissionsLauncher, new String[]{ Manifest.permission.CAMERA} )
                        .show(requireContext());
                if( hasPermissions ){
                    startScanner();
                }
            });
        }

        mSuggestedProductsViewModel.onProductVote()
                .observe(getViewLifecycleOwner(), mSharedViewModel::setProduct);
        mSharedViewModel.getProduct()
                .observe(getViewLifecycleOwner(), getViewModel()::setProduct);

        InputUtil.FieldTextWatcher[] barcodeWatcher = new InputUtil.FieldTextWatcher[1];

        getViewModel().getProduct().observe(getViewLifecycleOwner(), productWithTags -> {
            if( productWithTags != null ) {
                if( barcodeWatcher[0] != null ){
                    editBarcode.removeTextChangedListener(barcodeWatcher[0]);
                }

                barcodeWatcher[0] = new InputUtil.FieldTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        mSharedViewModel.setProduct(null);
                        productForm.setVisibility( View.GONE );
                    }
                };

                editBarcode.addTextChangedListener( barcodeWatcher[0] );

                productForm.setVisibility( View.VISIBLE );
                // close the dialog because user select a product
                closeBottomSheetDialog(view);
            }
        });

        getViewModel().onSave().observe(getViewLifecycleOwner(), shouldSave -> {
            if( !shouldSave ) return;


        });
    }

    private void search(String barcode) {
        mViewModel.setBarcode( barcode );
        openBottomSheetDialog(getView());
    }

    private void openBottomSheetDialog(View view) {
        NavController navController = NavHostFragment.findNavController( this );
        Log.e( TAG , "OPENING");
        Log.e( TAG , "CD: " + navController.getCurrentDestination());


        LiveData<String> mBarcode = mViewModel.getBarcode();
        mBarcode.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String barcode) {
                mBarcode.removeObserver(this);
                if( navController
                        .getCurrentDestination()
                        .getId() == R.id.registerProductFragment )
                {
                    RegisterProductFragmentDirections.openSuggestedProducts(barcode);
                }
            }
        });
    }

    private void closeBottomSheetDialog(View view) {
        NavController navController = NavHostFragment.findNavController( this );
        Log.e( TAG , "CLOSING");
        Log.e( TAG , "CD: " + navController.getCurrentDestination());
        if( navController
                .getCurrentDestination()
                .getId() == R.id.suggestedProductListDialogFragment )
        {
            navController
                    .popBackStack(R.id.suggestedProductListDialogFragment, true);
        }
    }

    private void startScanner() {
        try {
            scanLauncher.launch(new Intent(getContext(), BarcodeScannerActivity.class));
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Unable to scan barcode", Toast.LENGTH_LONG ).show();
        }
    }
}