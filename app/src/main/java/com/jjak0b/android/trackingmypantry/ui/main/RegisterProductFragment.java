package com.jjak0b.android.trackingmypantry.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Product;

public class RegisterProductFragment extends Fragment {

    private RegisterProductViewModel mViewModel;
    final String ARG_BARCODE = "barcode";

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

        mViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);

        final EditText editBarcode = view.findViewById(R.id.editTextBarcode);
        final Button submitBarcodeBtn = view.findViewById( R.id.submitBarcode );
        final EditText editName = view.findViewById( R.id.editProductName );
        final EditText editDescription = view.findViewById( R.id.editProductDescription );
        final Button submitProductBtn = view.findViewById( R.id.submitRegisterProductBtn );
        final View productForm = view.findViewById(R.id.productForm);

        LiveData<Product.Builder> productBuilder = mViewModel.getProductBuilder();
        productBuilder.observe(getViewLifecycleOwner(), builder -> {
            closeBottomSheetDialog(view);

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
                Product product = builder.build();
            });

            productForm.setVisibility( View.VISIBLE );
        });

        submitBarcodeBtn.setOnClickListener(v -> {
            mViewModel.setBarcode( editBarcode.getText().toString() );
        });

        mViewModel.getBarcode().observe( getViewLifecycleOwner(), value -> {
            editBarcode.setText( value );
        });

        mViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if( products != null && !products.isEmpty() ) {
                productForm.setVisibility( View.GONE );
                openBottomSheetDialog(view);
            }
        });

        Bundle args = getArguments();
        if( args != null ) {
            String barcode = args.getString(ARG_BARCODE);
            if (barcode != null) mViewModel.setBarcode(barcode);
        }

    }


    private void openBottomSheetDialog(View view) {
        Bundle bottomSheetBundle = new Bundle();
        bottomSheetBundle.putString( ARG_BARCODE, mViewModel.getBarcode().getValue() );

        Navigation.findNavController(view)
                .navigate( R.id.action_registerProductFragment_to_suggestedProductListDialogFragment, bottomSheetBundle );

    }

    private void closeBottomSheetDialog(View view) {
        Navigation.findNavController(view)
                .popBackStack(R.id.registerProductFragment, false);
    }

    private abstract class FieldTextWatcher implements TextWatcher {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        public abstract void afterTextChanged(Editable s);
    }
}