package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.util.ImageUtil;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;
import com.jjak0b.android.trackingmypantry.ui.util.LoadUtil;
import com.jjak0b.android.trackingmypantry.ui.util.Permissions;

public class ProductInfoFragment extends Fragment {

    protected ProductInfoViewModel mViewModel;
    protected ActivityResultLauncher<Void> takePictureLauncher;
    protected ActivityResultLauncher<String[]> requestCameraPermissionsLauncher;

    @DrawableRes
    protected static final int RESOURCE_DEFAULT_PRODUCT_IMG = R.drawable.ic_baseline_product_placeholder;
    protected Drawable LOADING_PLACEHOLDER;

    public static ProductInfoFragment newInstance() {
        return new ProductInfoFragment();
    }

    protected ProductInfoViewModel initViewModel() {
        return new ViewModelProvider(this).get(ProductInfoViewModel.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = initViewModel();

        takePictureLauncher = registerForActivityResult( new ImageUtil.ActivityResultContractTakePicture(), result -> {
            if( result != null ){
                mViewModel.setImage(result);
            }
        });

        requestCameraPermissionsLauncher = registerForActivityResult( new ActivityResultContracts.RequestMultiplePermissions(), areGranted -> {
            if( !areGranted.containsValue(false) ) {
                takePicture();
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

        LOADING_PLACEHOLDER = LoadUtil.getProgressLoader(requireContext());
        final TextInputLayout barcodeInputLayout = view.findViewById(R.id.barcodeInputLayout);
        final EditText editBarcode = (EditText) view.findViewById(R.id.editTextBarcode);
        final TextInputLayout nameInputLayout = view.findViewById(R.id.productNameInputLayout);
        final EditText editName = (EditText) view.findViewById( R.id.editProductName );
        final TextInputLayout descriptionInputLayout = view.findViewById(R.id.productDescriptionInputLayout);
        final EditText editDescription = (EditText) view.findViewById( R.id.editProductDescription );

        final ImageButton photoPreviewBtn = (ImageButton) view.findViewById(R.id.photoPreviewBtn);
        final ImageView photoPreview = (ImageView) view.findViewById(R.id.photoPreview);
        final View sectionTakePhoto = (View) view.findViewById(R.id.photoPreviewBtn);

        boolean hasFeatureCamera = getContext().getPackageManager()
                .hasSystemFeature( PackageManager.FEATURE_CAMERA_ANY );

        if( !hasFeatureCamera ){
            sectionTakePhoto.setVisibility( View.GONE );
        }
        else {
            photoPreviewBtn.setOnClickListener(v -> {
                boolean hasPermissions = new Permissions.FeatureRequestBuilder()
                        .setRationaleMessage(R.string.rationale_msg_features_take_photo)
                        .setOnPositive(requestCameraPermissionsLauncher, new String[]{Manifest.permission.CAMERA} )
                        .show(requireContext());
                if( hasPermissions ){
                    takePicture();
                }
            });
        }

        editBarcode.addTextChangedListener( new InputUtil.FieldTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setBarcode(s.toString());
            }
        });
        editName.addTextChangedListener( new InputUtil.FieldTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setName(s.toString());
            }
        });
        editDescription.addTextChangedListener( new InputUtil.FieldTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setDescription(s.toString());
            }
        });

        mViewModel.getBarcode().observe(getViewLifecycleOwner(), resource -> {
            InputUtil.setText(editBarcode, resource.getData());
            switch (resource.getStatus()) {
                case ERROR:
                    barcodeInputLayout.setError(resource.getError().getLocalizedMessage());
                    break;
                default:
                    barcodeInputLayout.setError(null);
            }
        });

        mViewModel.getName().observe(getViewLifecycleOwner(), resource -> {
            InputUtil.setText(editName, resource.getData());
            switch (resource.getStatus()) {
                case ERROR:
                    nameInputLayout.setError(resource.getError().getLocalizedMessage());
                    break;
                default:
                    nameInputLayout.setError(null);
            }
        });

        mViewModel.getDescription().observe(getViewLifecycleOwner(), resource -> {
            InputUtil.setText(editDescription, resource.getData());
            switch (resource.getStatus()) {
                case ERROR:
                    descriptionInputLayout.setError(resource.getError().getLocalizedMessage());
                    break;
                default:
                    descriptionInputLayout.setError(null);
            }
        });

        mViewModel.getImage().observe( getViewLifecycleOwner(), resource -> {
            Glide.with(view)
                    .load(resource.getData())
                    .fitCenter()
                    .placeholder(LOADING_PLACEHOLDER)
                    .fallback(RESOURCE_DEFAULT_PRODUCT_IMG)
                    .into(photoPreview);
        });

        mViewModel.onSave().observe( getViewLifecycleOwner(), shouldSave -> {
            if( !shouldSave ) return;

            // close any open keyboard
            InputUtil.hideKeyboard(requireActivity());
        });
    }

    private void takePicture() {
        try {
            takePictureLauncher.launch(null);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), R.string.error_unable_to_take_picture, Toast.LENGTH_LONG ).show();
        }
    }
}