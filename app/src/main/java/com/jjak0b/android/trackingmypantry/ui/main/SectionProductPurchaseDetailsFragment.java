package com.jjak0b.android.trackingmypantry.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjak0b.android.trackingmypantry.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SectionProductPurchaseDetailsFragment extends Fragment {

    public SectionProductPurchaseDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_section_product_purchase_details, container, false);
    }
}