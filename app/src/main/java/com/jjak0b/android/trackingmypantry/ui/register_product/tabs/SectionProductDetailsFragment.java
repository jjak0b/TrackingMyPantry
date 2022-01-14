package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.jjak0b.android.trackingmypantry.BarcodeScannerActivity;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.ui.products.ProductViewHolder;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductFragmentDirections;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.register_product.SharedProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ChipTagUtil;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;
import com.jjak0b.android.trackingmypantry.ui.util.Permissions;

import java.util.ArrayList;

public class SectionProductDetailsFragment extends Fragment {

    private SharedProductViewModel mProductPickerViewModel;
    private RegisterProductViewModel mSharedViewModel;
    private SectionProductDetailsViewModel mViewModel;

    static final String TAG = "SectionProductDetailsProductFragment";
    private ActivityResultLauncher<Intent> scanLauncher;
    private ActivityResultLauncher<String[]> requestScanCameraPermissionsLauncher;

    @NonNull
    public SectionProductDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(SectionProductDetailsViewModel.class);
    }

    @NonNull
    private SectionProductDetailsViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = initViewModel();
        mSharedViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
        mProductPickerViewModel = new ViewModelProvider(requireActivity()).get(SharedProductViewModel.class);

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

        setupSave(view, savedInstanceState);
        setupReset(view, savedInstanceState);
        setupSearch(view, savedInstanceState);
        setupProduct(view, savedInstanceState);
        setupTags(view, savedInstanceState);
    }

    public void setupSave(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getViewModel().canSave().observe(getViewLifecycleOwner(), canSave -> {
            // Log.d(TAG, "canSave=" +canSave);
            if( canSave ) getViewModel().save();
        });

        mSharedViewModel.onSaveProductDetails().observe(getViewLifecycleOwner(), isSaving -> {
            if( !isSaving ) return;
            Log.d(TAG, "force saving");
            getViewModel().save();
        });

        getViewModel().onSave().observe( getViewLifecycleOwner(), shouldSave -> {
            Log.d(TAG, "isSaving=" +shouldSave);
            if( !shouldSave ) return;

            // close any open keyboard
            InputUtil.hideKeyboard(requireActivity());

            getViewModel().saveComplete();
        });

        getViewModel().onSaved().observe(getViewLifecycleOwner(), resource -> {
            Log.d(TAG, "Saved Product details: " + resource );
            mSharedViewModel.setProductDetails(resource);
            switch (resource.getStatus()) {
                case ERROR:
                    new AlertDialog.Builder(requireContext())
                            .setPositiveButton(android.R.string.ok, null)
                            .setMessage(ErrorsUtils.getErrorMessage(requireContext(), resource.getError(), TAG))
                            .show();
                    break;
            }
        });
    }

    public void setupReset(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mSharedViewModel.onReset().observe(getViewLifecycleOwner(), shouldReset -> {
            if( shouldReset ) {
                Log.d(TAG, "Resetting");
                getViewModel().reset();
                // mProductPickerViewModel.setProductSource(null);
            }
        });
    }

    private void setupSearch(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final TextInputLayout barcodeInputLayout = view.findViewById(R.id.barcodeInputLayout);
        final EditText editBarcode = (EditText) view.findViewById(R.id.editTextBarcode);
        final View productForm = view.findViewById(R.id.productForm);

        boolean hasFeatureCamera = getContext().getPackageManager()
                .hasSystemFeature( PackageManager.FEATURE_CAMERA_ANY );

        barcodeInputLayout.setEnabled(true);
        barcodeInputLayout.setStartIconVisible(true);
        barcodeInputLayout.setEndIconVisible(hasFeatureCamera);
        barcodeInputLayout.setErrorIconDrawable(null);

        editBarcode.addTextChangedListener( new InputUtil.FieldTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                getViewModel().setBarcode(s.toString());
            }
        });

        getViewModel().getBarcode().observe(getViewLifecycleOwner(), resource -> {
            editBarcode.setText(resource.getData());
            editBarcode.setSelection(editBarcode.length());

            switch (resource.getStatus()) {
                case LOADING:
                    barcodeInputLayout.setError(null);
                    barcodeInputLayout.setStartIconOnClickListener(null);
                    break;
                case SUCCESS:
                    barcodeInputLayout.setError(null);
                    barcodeInputLayout.setStartIconOnClickListener(v -> {
                        search( resource.getData() );
                    });
                    break;
                case ERROR:
                    barcodeInputLayout.setStartIconOnClickListener(null);
                    if( resource.getError() instanceof FormException ) {
                        barcodeInputLayout.setError(resource.getError().getLocalizedMessage());
                    }
                    break;
            }
        });

        editBarcode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(editBarcode.getText().toString());
                return true;
            }
            return false;
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

        InputUtil.FieldTextWatcher[] barcodeWatcher = new InputUtil.FieldTextWatcher[1];

        getViewModel().getProduct().observe(getViewLifecycleOwner(), productResource -> {
            // when a new product is set, reset the product if barcode change
            if( productResource.getStatus() == Status.SUCCESS ) {

                // remove old text listener this case
                if( barcodeWatcher[0] != null ){
                    editBarcode.removeTextChangedListener(barcodeWatcher[0]);
                }

                // create and add a new text listener to reset product on barcode change
                barcodeWatcher[0] = new InputUtil.FieldTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        // reset picked product
                        mProductPickerViewModel.setProductSource(null);
                        productForm.setVisibility( View.GONE );
                    }
                };
                editBarcode.addTextChangedListener( barcodeWatcher[0] );

                // close the dialog because user select a product
                // closeBottomSheetDialog(view);
            }
        });
    }

    private void setupProduct(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final View productContainer = view.findViewById(R.id.productForm);
        final ImageButton removeProductBtn = view.findViewById(R.id.discardProductBtn);
        final ViewGroup productPreviewContainer = view.findViewById(R.id.productPreviewInnerContainer);
        ProductViewHolder previewHolder = ProductViewHolder.create(productPreviewContainer);
        productPreviewContainer.addView(previewHolder.itemView);

        removeProductBtn.setOnClickListener(v -> {
            Log.d(TAG, "Unset picked product" );
            mProductPickerViewModel.setProductSource(null);
        });

        getViewModel().getProduct().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case ERROR:
                    Log.e(TAG, "Error on product changed: " , resource.getError() );
                    productContainer.setVisibility(View.GONE);
                    new AlertDialog.Builder(requireContext())
                            .setPositiveButton(android.R.string.ok, null)
                            .setMessage(ErrorsUtils.getErrorMessage(requireContext(), resource.getError(), TAG) )
                            .show();
                    break;
                case SUCCESS:
                    Log.d(TAG, "User picked: " + resource.getData() );
                    productContainer.setVisibility(View.VISIBLE);

                    ProductWithTags model = new ProductWithTags();
                    model.product = resource.getData();
                    model.tags = new ArrayList<>(0);

                    previewHolder.bind(model);

                    // trigger research on preview lick
                    previewHolder.itemView.setOnClickListener(v -> {
                        search(model.product.getBarcode());
                    });
                    break;
                default:
                    Log.d(TAG, "waiting for user to pick ...");
                    productContainer.setVisibility(View.GONE);
                    break;
            }

            if( resource.getData() == null ) {
                mSharedViewModel.setProductDetails(Resource.loading(null));
            }
        });

        // on product picked
        mProductPickerViewModel.getProduct().observe(getViewLifecycleOwner(), resource -> {
            getViewModel().setProduct(resource.getData());
        });
    }

    private void setupTags(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final NachoTextView chipsInput = (NachoTextView) view.findViewById(R.id.chips_input);
        final TextInputLayout tagsInputLayout = view.findViewById(R.id.tagsInputLayout);

        ArrayAdapter<ProductTag> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item);
        chipsInput.setAdapter( adapter );
        chipsInput.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR );
        chipsInput.setOnFocusChangeListener( (v, hasFocus) -> {
            if( !hasFocus ){
                getViewModel().setAssignedTags( ChipTagUtil.newTagsInstanceFromChips( chipsInput.getAllChips() ) );
            }
        });

        getViewModel().getAssignedTags().observe( getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    tagsInputLayout.setError(null);
                    chipsInput.setTextWithChips( ChipTagUtil.newChipsInstanceFromTags( resource.getData() ) );
                    chipsInput.setSelection(chipsInput.getText().length());
                    break;
                case ERROR:
                    if( resource.getError() instanceof FormException){
                        tagsInputLayout.setError(((FormException) resource.getError()).getLocalizedMessage(requireContext()));
                    }

                    break;
            }
        });

        getViewModel().getSuggestionTags().observe( getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    adapter.addAll( resource.getData() );
                    break;
                default:
                    adapter.clear();
                    break;
            }
        });


        getViewModel().onSave().observe( getViewLifecycleOwner(), shouldSave -> {
            if( !shouldSave ) return;

            // setOnFocusChangeListener of chips tags view is not triggered while clicking on a "save" view
            // so trigger it manually
            if (chipsInput.hasFocus()) {
                chipsInput.clearFocus();
                // mViewModel.setAssignedTags(ChipTagUtil.newTagsInstanceFromChips(chipsInput.getAllChips()));
            }
        });
    }

    private void search(String barcode) {
        getViewModel().setBarcode( barcode );
        openBottomSheetDialog(getView(), barcode);
    }

    private void openBottomSheetDialog(View view, String barcode) {
        NavController navController = NavHostFragment.findNavController( this );

            navController
                    .navigate(RegisterProductFragmentDirections.openSuggestedProducts(barcode));
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