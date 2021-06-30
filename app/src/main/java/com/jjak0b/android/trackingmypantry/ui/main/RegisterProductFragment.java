package com.jjak0b.android.trackingmypantry.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jjak0b.android.trackingmypantry.ImageUtil;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Product;

public class RegisterProductFragment extends Fragment {

    private RegisterProductViewModel mViewModel;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.e( "RegisterProductFragment", "onViewCreated");
        Log.e( "TEST", getViewLifecycleOwner().toString() );
        mViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
        Bundle args = getArguments();
        if( args != null ) {
            String barcode = args.getString(ARG_BARCODE);
            if (barcode != null) {
                Log.e( "RegisterProductFragment", "barcode "  + barcode);
                mViewModel.setBarcode(barcode);
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

        submitBarcodeBtn.setOnClickListener(v -> {
            mViewModel.setBarcode( editBarcode.getText().toString() );
        });

        mViewModel.getBarcode().observe( getViewLifecycleOwner(), value -> {
            Log.e( "RegisterProductFragment", "updated barcode "  + value);
            editBarcode.setText( value );
        });

        mViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            Log.e( "RegisterProductFragment", "updated products");
            if( products != null && !products.isEmpty() ) {
                productForm.setVisibility( View.GONE );
                openBottomSheetDialog(view);
                Log.e( "test", "OPEN bottom sheet");
            }
        });


        boolean hasFeatureCamera = getContext().getPackageManager()
                .hasSystemFeature( PackageManager.FEATURE_CAMERA_ANY );
        if( !hasFeatureCamera ){
            sectionTakePhoto.setVisibility( View.GONE );
        }


        LiveData<Product.Builder> productBuilder = mViewModel.getProductBuilder();

        productBuilder.observe(getViewLifecycleOwner(), builder -> {
            closeBottomSheetDialog(view);

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
                photoPreviewBtn.setImageBitmap( ImageUtil.convert( builder.getImg() ) );
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

            submitProductBtn.setOnClickListener(v -> {
                mViewModel.registerProduct()
                        .thenAccept(aVoid -> {
                            Toast.makeText(getContext(), "Register product successfully", Toast.LENGTH_LONG ).show();
                            Navigation.findNavController(view)
                                    .popBackStack(R.id.registerProductFragment, true);
                        })
                        .exceptionally(throwable -> {
                            Toast.makeText(getContext(), "Unable to register", Toast.LENGTH_SHORT ).show();
                            return null;
                        });
            });

            productForm.setVisibility( View.VISIBLE );
        });

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

        if( Navigation.findNavController(view)
                .getCurrentDestination()
                .getId() == R.id.registerProductFragment )
        {
            Navigation.findNavController(view)
                    .navigate( R.id.action_registerProductFragment_to_suggestedProductListDialogFragment, bottomSheetBundle );
        }
    }

    private void closeBottomSheetDialog(View view) {
        if( Navigation.findNavController(view)
                .getCurrentDestination()
                .getId() == R.id.suggestedProductListDialogFragment )
        {
            Navigation.findNavController(view)
                    .popBackStack(R.id.suggestedProductListDialogFragment, true);
        }
    }

    private abstract class FieldTextWatcher implements TextWatcher {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        public abstract void afterTextChanged(Editable s);
    }
}