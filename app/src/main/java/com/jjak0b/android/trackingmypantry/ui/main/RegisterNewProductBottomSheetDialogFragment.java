package com.jjak0b.android.trackingmypantry.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjak0b.android.trackingmypantry.BarcodeScannerActivity;
import com.jjak0b.android.trackingmypantry.R;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     RegisterProductMenuItemListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class RegisterNewProductBottomSheetDialogFragment extends BottomSheetDialogFragment {

    final int BARCODE_SCAN_REQUEST = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_register_new_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );

        LinearLayout menuItemScan = (LinearLayout) view.findViewById(R.id.section_scan);
        LinearLayout menuItemDigit = (LinearLayout) view.findViewById(R.id.section_digit);

        if (this.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            menuItemScan.setOnClickListener(v -> {
                Intent myIntent = new Intent(getActivity(), BarcodeScannerActivity.class);
                this.startActivityForResult(myIntent, BARCODE_SCAN_REQUEST);
            });
        }
        else {
            menuItemScan.setVisibility( View.GONE );
        }

        menuItemDigit.setOnClickListener(v -> navigateToRegisterProductForm(null) );
    }

    void navigateToRegisterProductForm( Bundle args ) {
        NavHostFragment.findNavController(this)
                .navigate( R.id.action_registerProductBottomSheetDialogFragment_to_registerProductFragment, args );
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch ( requestCode ) {
            case BARCODE_SCAN_REQUEST:
                if( resultCode == Activity.RESULT_OK ){
                    Bundle param = null;
                    String barcode = data.getStringExtra(BarcodeScannerActivity.BARCODE);
                    if( barcode != null ) {
                        param = new Bundle();
                        param.putString( "barcode" , barcode );
                    }

                    navigateToRegisterProductForm( param );
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}