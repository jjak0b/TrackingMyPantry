package com.jjak0b.android.trackingmypantry.ui.products.details;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.ui.util.ChipTagUtil;
import com.jjak0b.android.trackingmypantry.ui.util.ImageUtil;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;
import com.jjak0b.android.trackingmypantry.ui.util.Permissions;

public class ProductDetailsFragment extends Fragment {

    protected ProductDetailsViewModel mViewModel;
    protected ActivityResultLauncher<Void> takePictureLauncher;
    protected ActivityResultLauncher<String[]> requestCameraPermissionsLauncher;
    @DrawableRes
    protected static final int RESOURCE_LOADING_PRODUCT_IMG = R.drawable.loading_spinner;
    @DrawableRes
    protected static final int RESOURCE_DEFAULT_PRODUCT_IMG = R.drawable.ic_baseline_product_placeholder;


    public static ProductDetailsFragment newInstance() {
        return new ProductDetailsFragment();
    }

    protected ProductDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(ProductDetailsViewModel.class);
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

        final EditText editBarcode = (EditText) view.findViewById(R.id.editTextBarcode);
        final EditText editName = (EditText) view.findViewById( R.id.editProductName );
        final EditText editDescription = (EditText) view.findViewById( R.id.editProductDescription );
        final View productForm = view.findViewById(R.id.productForm);
        final ImageButton photoPreviewBtn = (ImageButton) view.findViewById(R.id.photoPreviewBtn);
        final ImageView photoPreview = (ImageView) view.findViewById(R.id.photoPreview);
        final View sectionTakePhoto = (View) view.findViewById(R.id.photoPreviewBtn);
        final NachoTextView chipsInput = (NachoTextView) view.findViewById(R.id.chips_input);

        productForm.setVisibility(View.VISIBLE);
        boolean hasFeatureCamera = getContext().getPackageManager()
                .hasSystemFeature( PackageManager.FEATURE_CAMERA_ANY );

        if( !hasFeatureCamera ){
            sectionTakePhoto.setVisibility( View.GONE );
        }
        else {
            photoPreviewBtn.setOnClickListener(v -> {
                boolean hasPermissions = new Permissions.FeatureRequestBuilder()
                        .setRationaleMessage(R.string.register_product_take_photo)
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

        ArrayAdapter<ProductTag> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item);
        chipsInput.setAdapter( adapter );
        chipsInput.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR );
        chipsInput.setOnFocusChangeListener( (v, hasFocus) -> {
            if( !hasFocus ){
                mViewModel.setAssignedTags( ChipTagUtil.newTagsInstanceFromChips( chipsInput.getAllChips() ) );
            }
        });

        mViewModel.getBarcode().observe(getViewLifecycleOwner(), s -> {
            editBarcode.setText(s);
            editBarcode.setSelection(s.length());
        });

        mViewModel.getName().observe(getViewLifecycleOwner(), s -> {
            editName.setText(s);
            editName.setSelection(s.length());
        });
        mViewModel.getDescription().observe(getViewLifecycleOwner(), s -> {
            editDescription.setText(s);
            editDescription.setSelection(s.length());
        });
        mViewModel.getImage().observe( getViewLifecycleOwner(), bitmap -> {
            Glide.with(view)
                    .load(bitmap)
                    .fitCenter()
                    .placeholder(RESOURCE_LOADING_PRODUCT_IMG)
                    .fallback(RESOURCE_DEFAULT_PRODUCT_IMG)
                    .into(photoPreview);
        });
        mViewModel.getAssignedTags().observe( getViewLifecycleOwner(), productTags -> {
            chipsInput.setTextWithChips( ChipTagUtil.newChipsInstanceFromTags( productTags ) );
            chipsInput.setSelection(chipsInput.getText().length());
        });
        mViewModel.getSuggestionTags().observe( getViewLifecycleOwner(), productTags -> {
            adapter.clear();
            adapter.addAll( productTags );
        });

        mViewModel.onSave().observe( getViewLifecycleOwner(), shouldSave -> {
            if( !shouldSave ) return;

            // close any open keyboard
            InputUtil.hideKeyboard(requireActivity());

            // setOnFocusChangeListener of chips tags view is not triggered while clicking on a "save" view
            // so trigger it manually
            if (chipsInput.hasFocus()) {
                chipsInput.clearFocus();
                // mViewModel.setAssignedTags(ChipTagUtil.newTagsInstanceFromChips(chipsInput.getAllChips()));
            }
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