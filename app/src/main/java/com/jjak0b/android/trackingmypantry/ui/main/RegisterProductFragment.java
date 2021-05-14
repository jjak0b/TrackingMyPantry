package com.jjak0b.android.trackingmypantry.ui.main;

import androidx.core.app.BundleCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
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


        editBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setBarcode( s.toString() );
            }
        });

        submitBarcodeBtn.setOnClickListener(v -> openBottomSheetDialog(view) );

        mViewModel.getBarcode().observe( getViewLifecycleOwner(), value -> {
            editBarcode.setText( value );
        });


        Bundle args = getArguments();
        if( args != null ) {
            String barcode = args.getString(ARG_BARCODE);
            if (barcode != null) mViewModel.setBarcode(args.getString(ARG_BARCODE));
        }
    }


    private void openBottomSheetDialog(View view) {
        Bundle bottomSheetBundle = new Bundle();
        bottomSheetBundle.putString( ARG_BARCODE, mViewModel.getBarcode().getValue() );

        Navigation.findNavController(view)
                .navigate( R.id.action_registerProductFragment_to_suggestedProductListDialogFragment, bottomSheetBundle );

    }

}