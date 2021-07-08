package com.jjak0b.android.trackingmypantry.ui.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ListAdapter;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.chip.ChipInfo;
import com.hootsuite.nachos.chip.ChipSpan;
import com.hootsuite.nachos.chip.ChipSpanChipCreator;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer;
import com.jjak0b.android.trackingmypantry.ImageUtil;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.auth.AuthException;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.ui.util.ChipTagUtil;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java9.util.function.BiConsumer;
import retrofit2.HttpException;

public class RegisterProductFragment extends Fragment {

    private RegisterProductViewModel mViewModel;
    static final String TAG = RegisterProductFragment.class.getName();
    final String ARG_BARCODE = "barcode";
    final int REQUEST_IMAGE_CAPTURE = 1;
    final int BITMAP_SIZE = 256;
    private ImageButton photoPreviewBtn;
    private final int RESOURCE_DEFAULT_PRODUCT_IMG = R.drawable.ic_baseline_product_placeholder;
    public static RegisterProductFragment newInstance() {
        return new RegisterProductFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register_product_fragment, container, false);
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

        final EditText editBarcode = view.findViewById(R.id.editTextBarcode);
        final Button submitBarcodeBtn = view.findViewById( R.id.submitBarcode );
        final EditText editName = view.findViewById( R.id.editProductName );
        final EditText editDescription = view.findViewById( R.id.editProductDescription );
        final Button submitProductBtn = view.findViewById( R.id.submitRegisterProductBtn );
        final View productForm = view.findViewById(R.id.productForm);
        photoPreviewBtn = view.findViewById(R.id.photoPreviewBtn);
        final ViewGroup sectionTakePhoto = view.findViewById(R.id.sectionTakePhoto);
        final NachoTextView chipsInput = view.findViewById(R.id.chips_input);

        chipsInput.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR );
        ArrayAdapter<ProductTag> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line
        );
        chipsInput.setAdapter( adapter );

        submitBarcodeBtn.setOnClickListener(v -> {
            mViewModel.setBarcode( editBarcode.getText().toString() );
        });

        mViewModel.getBarcode().observe( getViewLifecycleOwner(), value -> {
            Log.e( "RegisterProductFragment", "updated barcode "  + value);
            editBarcode.setText( value );
            mViewModel.setProduct(
                    new Product.Builder()
                            .setBarcode( value )
                            .build()
            );
        });

        boolean hasFeatureCamera = getContext().getPackageManager()
                .hasSystemFeature( PackageManager.FEATURE_CAMERA_ANY );
        if( !hasFeatureCamera ){
            sectionTakePhoto.setVisibility( View.GONE );
        }

        LiveData<Product.Builder> productBuilder = mViewModel.getProductBuilder();

        productBuilder.observe(getViewLifecycleOwner(), builder -> {

            if( hasFeatureCamera  ) {
                photoPreviewBtn.setOnClickListener(v -> {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    } catch (ActivityNotFoundException e) {
                        // TODO: display error state to the user: no available app to use to get a picture
                        Toast.makeText(getContext(), "Unable to take a photo", Toast.LENGTH_LONG ).show();
                    }
                });
            }

            if( builder.getImg() != null ) {
                try {
                    photoPreviewBtn.setImageBitmap( ImageUtil.convert( builder.getImg() ) );
                }
                catch ( IllegalArgumentException exception ) {
                    photoPreviewBtn.setImageResource( RESOURCE_DEFAULT_PRODUCT_IMG );
                }
            }
            else {
                photoPreviewBtn.setImageResource( RESOURCE_DEFAULT_PRODUCT_IMG );
            }

            editBarcode.setText( builder.getBarcode() );
            editName.setText( builder.getName() );
            editDescription.setText( builder.getDescription() );

            editBarcode.addTextChangedListener(new FieldTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    productForm.setVisibility( View.GONE );
                }
            });

            editName.addTextChangedListener(new FieldTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    builder.setName(s.toString());
                }
            });

            editDescription.addTextChangedListener(new FieldTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    builder.setDescription(s.toString());
                }
            });

            // submit product
            submitProductBtn.setOnClickListener(v -> {

                mViewModel.setAssignedTags( ChipTagUtil.newTagsInstanceFromChips( chipsInput.getAllChips() ) );

                Futures.addCallback(
                        mViewModel.registerProduct(),
                        new FutureCallback<Object>() {
                            @Override
                            public void onSuccess(@NullableDecl Object result) {
                                Toast.makeText(getContext(), "Register product successfully", Toast.LENGTH_LONG ).show();
                                Navigation.findNavController(view)
                                        .popBackStack(R.id.registerProductFragment, true);
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                if( t instanceof AuthException ){
                                    Log.e( TAG, "Authentication Error", t );
                                    Toast.makeText(getContext(), "Authentication Error: You need to login first", Toast.LENGTH_SHORT )
                                            .show();
                                }
                                else if( t instanceof HttpException ){
                                    Log.e( TAG, "Server Error", t );
                                    Toast.makeText(getContext(), "Server error: Unable to add to the server", Toast.LENGTH_SHORT )
                                            .show();
                                }
                                else if( t instanceof IOException ){
                                    Log.e( TAG, "Network Error", t );
                                    Toast.makeText(getContext(), "Network error: Unable to connect to server", Toast.LENGTH_SHORT )
                                            .show();
                                }
                                else {
                                    Log.e( TAG, "Unexpected Error", t );
                                    Toast.makeText(getContext(), "Unexpected error: Unable to perform operation", Toast.LENGTH_SHORT )
                                            .show();
                                }
                            }
                        },
                        ContextCompat.getMainExecutor( getContext() )
                );
            });

            productForm.setVisibility( View.VISIBLE );


            // TODO: check product id in DB and get current tags

            closeBottomSheetDialog(view);
        });


        mViewModel.getAssignedTags().observe( getViewLifecycleOwner(), productTags -> {
            chipsInput.setTextWithChips( ChipTagUtil.newChipsInstanceFromTags( productTags ) );
        });

        mViewModel.getSuggestionTags().observe( getViewLifecycleOwner(), productTags -> {
            adapter.clear();
            adapter.addAll( productTags );
        });

        mViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            Log.e( "RegisterProductFragment", "updated products");
            if( products != null && !products.isEmpty() ) {
                productForm.setVisibility( View.GONE );
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK ) {
            Bundle extras = data.getExtras();

            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if( imageBitmap != null ){
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, BITMAP_SIZE, BITMAP_SIZE, false);
                photoPreviewBtn.setImageBitmap( imageBitmap );
                mViewModel.getProductBuilder().getValue()
                        .setImg( ImageUtil.convert( imageBitmap ) );
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

    private abstract class FieldTextWatcher implements TextWatcher {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        public abstract void afterTextChanged(Editable s);
    }
}