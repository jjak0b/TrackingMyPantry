package com.jjak0b.android.trackingmypantry.ui.main;

import androidx.core.app.BundleCompat;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        mViewModel = new ViewModelProvider(this).get(RegisterProductViewModel.class);
        // TODO: Use the ViewModel

        Bundle args = getArguments();
        EditText editBarcode = view.findViewById(R.id.editTextBarcode);

        if( args != null ) {
            String barcode = args.getString( ARG_BARCODE );
            if( barcode != null ) {
                editBarcode.setText( barcode );
            }
        }


        Bundle bottomSheetBundle = new Bundle();
        bottomSheetBundle.putInt( "item_count", 6 );


        Navigation.findNavController(view)
                .navigate( R.id.action_registerProductFragment_to_suggestedProductListDialogFragment, bottomSheetBundle );
    }

}