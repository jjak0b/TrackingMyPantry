package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.jjak0b.android.trackingmypantry.BarcodeScannerActivity;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ImageUtil;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.ui.util.ChipTagUtil;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

public class SectionProductDetailsFragment extends Fragment {

    private RegisterProductViewModel mViewModel;
    static final String TAG = SectionProductDetailsFragment.class.getName();
    final String ARG_BARCODE = "barcode";
    final int REQUEST_BARCODE_SCAN = 2;
    final int REQUEST_IMAGE_CAPTURE = 1;
    final int BITMAP_SIZE = 256;
    private ImageView photoPreview;
    private final int RESOURCE_DEFAULT_PRODUCT_IMG = R.drawable.ic_baseline_product_placeholder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_section_product_details, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d( "RegisterProductFragment", "onDestroyView");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.e( "RegisterProductFragment", "onViewCreated");
        Log.e( "TEST", getViewLifecycleOwner().toString() );
        mViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
        Bundle args = getArguments();
        if( args != null ) {
            String barcode = args.getString(ARG_BARCODE);
            if (barcode != null) {
                if( !barcode.equals( mViewModel.getBarcode().getValue() ) ) {
                    Log.e("RegisterProductFragment", "barcode " + barcode);
                    // mViewModel.setBarcode(null);
                    mViewModel.setBarcode(barcode);
                }
            }
        }

        final TextInputLayout barcodeInputLayout = view.findViewById(R.id.barcodeInputLayout);
        final EditText editBarcode = (EditText) view.findViewById(R.id.editTextBarcode);
        final EditText editName = (EditText) view.findViewById( R.id.editProductName );
        final EditText editDescription = (EditText) view.findViewById( R.id.editProductDescription );
        final View productForm = view.findViewById(R.id.productForm);
        final ImageButton photoPreviewBtn = (ImageButton) view.findViewById(R.id.photoPreviewBtn);
        photoPreview = (ImageView) view.findViewById(R.id.photoPreview);
        final View sectionTakePhoto = (View) view.findViewById(R.id.photoPreviewBtn);
        final NachoTextView chipsInput = (NachoTextView) view.findViewById(R.id.chips_input);

        chipsInput.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR );
        ArrayAdapter<ProductTag> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line
        );
        chipsInput.setAdapter( adapter );
        chipsInput.setOnFocusChangeListener( (v, hasFocus) -> {
            if( !hasFocus ){
                mViewModel.setAssignedTags( ChipTagUtil.newTagsInstanceFromChips( chipsInput.getAllChips() ) );
            }
        });

        editBarcode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mViewModel.setBarcode( editBarcode.getText().toString() );
                return true;
            }
            return false;
        });

        barcodeInputLayout.setStartIconOnClickListener(v -> {
            mViewModel.setBarcode( editBarcode.getText().toString() );
        });

        mViewModel.getBarcode().observe( getViewLifecycleOwner(), value -> {
            Log.e( "RegisterProductFragment", "updated barcode "  + value);
            editBarcode.setText( value );
        });

        boolean hasFeatureCamera = getContext().getPackageManager()
                .hasSystemFeature( PackageManager.FEATURE_CAMERA_ANY );

        if( !hasFeatureCamera ){
            sectionTakePhoto.setVisibility( View.GONE );
        }
        else{
            photoPreviewBtn.setOnClickListener(v -> {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    // TODO: display error state to the user: no available app to use to get a picture
                    Toast.makeText(getContext(), "Unable to take a photo", Toast.LENGTH_LONG ).show();
                }
            });

            barcodeInputLayout.setEndIconOnClickListener( v -> {
                Intent scanIntent = new Intent(getActivity(), BarcodeScannerActivity.class);

                try {
                    this.startActivityForResult(scanIntent, REQUEST_BARCODE_SCAN);
                }
                catch (ActivityNotFoundException e) {
                    // TODO: display error state to the user: no available app to scan
                    Toast.makeText(getContext(), "Unable to scan barcode", Toast.LENGTH_LONG ).show();
                }
            });
        }

        LiveData<Product.Builder> productBuilder = mViewModel.getProductBuilder();

        // needs this to unregister watcher on new builder update
        // the indexes are for: editBarcode, editName, editDescription
        InputUtil.FieldTextWatcher[] registeredTextWatchers = new InputUtil.FieldTextWatcher[3];

        productBuilder.observe(getViewLifecycleOwner(), builder -> {

            editBarcode.removeTextChangedListener( registeredTextWatchers[ 0 ] );
            editName.removeTextChangedListener( registeredTextWatchers[ 1 ] );
            editDescription.removeTextChangedListener( registeredTextWatchers[ 2 ] );

            if( builder == null ){
                productForm.setVisibility( View.GONE );
                return;
            }

            if( builder.getBarcode() != null )
                editBarcode.setText( builder.getBarcode() );
            editName.setText( builder.getName() );
            editDescription.setText( builder.getDescription() );

            if( builder.getImg() != null ) {
                try {
                    photoPreview.setImageBitmap( ImageUtil.convert( builder.getImg() ) );
                }
                catch ( IllegalArgumentException exception ) {
                    photoPreview.setImageResource( RESOURCE_DEFAULT_PRODUCT_IMG );
                }
            }
            else {
                photoPreview.setImageResource( RESOURCE_DEFAULT_PRODUCT_IMG );
            }

            registeredTextWatchers[ 0 ] = new InputUtil.FieldTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if( !s.toString().equals( builder.getBarcode() )){
                        mViewModel.setProduct( null );
                        productForm.setVisibility( View.GONE );
                    }
                }
            };

            registeredTextWatchers[ 1 ] = new InputUtil.FieldTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    builder.setName(s.toString());
                }
            };

            registeredTextWatchers[ 2 ] = new InputUtil.FieldTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    builder.setDescription(s.toString());
                }
            };

            editBarcode.addTextChangedListener( registeredTextWatchers[ 0 ] );
            editName.addTextChangedListener( registeredTextWatchers[ 1 ] );
            editDescription.addTextChangedListener( registeredTextWatchers[ 2 ] );

            productForm.setVisibility( View.VISIBLE );


            // TODO: check product id in DB and get current tags
            // close the dialog because user select a product
            closeBottomSheetDialog(view);
        });


        mViewModel.getAssignedTags().observe( getViewLifecycleOwner(), productTags -> {
            chipsInput.setTextWithChips( ChipTagUtil.newChipsInstanceFromTags( productTags ) );
        });

        mViewModel.getSuggestionTags().observe( getViewLifecycleOwner(), productTags -> {
            adapter.clear();
            adapter.addAll( productTags );
        });

        mViewModel.onUpdateMatchingProductsList().observe(getViewLifecycleOwner(), products -> {
            Log.e( "RegisterProductFragment", "updated products");
            if( products != null ) {
                openBottomSheetDialog(view);
                Log.e( "test", "OPEN bottom sheet with " + products.size() + " elements" );
            }
        });
        /* if using this order instead:
            setBarcode
            getProducts().observe
            -> open
            productBuilder.observe
            -> close

            the close operation will be done so the virtual stack will be on "registerProductFragment"
            But visually there is still the "suggestedProductListDialog" on the screen ( navigation library bug ? ).
            So if a product will be selected won't popTo "registerProductFragment" because "Navigation" think is already at destination
            and if the back button will be pressed then Navigation will popOff the real "registerProductFragment".
            Investigate
         */
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK ) {
            Bundle extras = data.getExtras();

            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if( imageBitmap != null ){
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, BITMAP_SIZE, BITMAP_SIZE, false);
                photoPreview.setImageBitmap( imageBitmap );
                mViewModel.getProductBuilder().getValue()
                        .setImg( ImageUtil.convert( imageBitmap ) );
            }
        }
        else if( requestCode == REQUEST_BARCODE_SCAN ){
            if( resultCode == Activity.RESULT_OK ) {
                String barcode = data.getStringExtra(BarcodeScannerActivity.BARCODE);
                mViewModel.setBarcode( barcode );
            }
        }
    }

    private void openBottomSheetDialog(View view) {
        Bundle bottomSheetBundle = new Bundle();
        bottomSheetBundle.putString( ARG_BARCODE, mViewModel.getBarcode().getValue() );
        NavController navController = NavHostFragment.findNavController( this );
        Log.e( "RegiserPoductFragment" , "OPENING");
        Log.e( "RegiserPoductFragment" , "CD: " + navController.getCurrentDestination());

        if( navController
                .getCurrentDestination()
                .getId() == R.id.registerProductFragment )
        {
            navController
                    .navigate( R.id.action_registerProductFragment_to_suggestedProductListDialogFragment, bottomSheetBundle );
        }
    }

    private void closeBottomSheetDialog(View view) {
        NavController navController = NavHostFragment.findNavController( this );
        Log.e( "RegiserPoductFragment" , "CLOSING");
        Log.e( "RegiserPoductFragment" , "CD: " + navController.getCurrentDestination());
        if( navController
                .getCurrentDestination()
                .getId() == R.id.suggestedProductListDialogFragment )
        {
            navController
                    .popBackStack(R.id.suggestedProductListDialogFragment, true);
        }
    }
}