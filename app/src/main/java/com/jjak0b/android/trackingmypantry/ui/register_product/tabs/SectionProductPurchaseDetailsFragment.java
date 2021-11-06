package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;


import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.ui.maps.PlacesPluginActivity;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

import com.jjak0b.android.trackingmypantry.ui.util.PlaceAdapter;
import com.mapbox.geojson.Point;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class SectionProductPurchaseDetailsFragment extends Fragment {

    private RegisterProductViewModel mViewModel;

    private ActivityResultLauncher<Intent> locationPickerLauncher;
    public SectionProductPurchaseDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(requireActivity())
                .get(RegisterProductViewModel.class);

        locationPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            int resultCode = result.getResultCode();
            Intent data = result.getData();
            if (resultCode == Activity.RESULT_OK && data != null) {

                Log.e("CFeature: ", PlacesPluginActivity.getPlace(data).toJson());
                Place place = PlaceAdapter.from(PlacesPluginActivity.getPlace(data));

                Log.e("Feature: ", Point.fromJson(place.getFeature().geometry().toJson()).toJson() );

                mViewModel.setPurchasePlace(place);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                mViewModel.setPurchasePlace(null);
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_section_product_purchase_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat( getActivity() );

        TextInputLayout purchaseLocationLayout = view.findViewById( R.id.product_purchase_location);
        TextInputLayout purchaseDateLayout = view.findViewById( R.id.product_purchase_date);
        EditText editPurchaseDate = view.findViewById( R.id.editTextPurchaseDate);
        EditText editPurchaseLocation = view.findViewById( R.id.editTextLocation );
        EditText editPurchaseCost = view.findViewById( R.id.editTextCost );
        InputUtil.FieldTextWatcher[] fieldTextWatchers = new InputUtil.FieldTextWatcher[2];

        // setup location
        View.OnClickListener showLocationPickerOnClick = v -> {
            locationPickerLauncher.launch(new Intent(getContext(), PlacesPluginActivity.class));
        };

        editPurchaseLocation.setOnClickListener( showLocationPickerOnClick );
        purchaseLocationLayout.setStartIconOnClickListener( showLocationPickerOnClick );
        mViewModel.getPurchasePlace().observe( getViewLifecycleOwner(), place -> {
            if( place != null && place.getName() != null ) {
                String placeName = place.getName();
                editPurchaseLocation.setText(placeName);
                editPurchaseLocation.setSelection(placeName.length());
            }
            else {
                editPurchaseLocation.setText(null);
            }
        });

        fieldTextWatchers[1] = new InputUtil.FieldTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // set null on clear text
                if( s.length() < 1 ){
                    mViewModel.setPurchasePlace(null);
                }
            }
        };
        editPurchaseLocation.addTextChangedListener( fieldTextWatchers[1] );

        mViewModel.getProductPurchaseInfo().observe( getViewLifecycleOwner(), purchaseInfo -> {
            if( purchaseInfo == null ){
                // set default values in fields
                mViewModel.resetPurchaseInfo();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            editPurchaseCost.removeTextChangedListener( fieldTextWatchers[0] );

            View.OnClickListener showDatePickerOnClick = new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    // date picker dialog
                    DatePickerDialog picker = new DatePickerDialog(getContext(),
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, monthOfYear);
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                                    calendar.set(Calendar.MINUTE, 0);
                                    calendar.set(Calendar.SECOND, 0);
                                    calendar.set(Calendar.MILLISECOND, 0);
                                    Date date = calendar.getTime();

                                    purchaseInfo.setPurchaseDate(date);
                                    editPurchaseDate.setText(dateFormat.format(date));
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    picker.show();
                }
            };

            fieldTextWatchers[0] = new InputUtil.FieldTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    float cost;
                    if( s.toString() == null ){
                        cost = 0f;
                    }
                    else {
                        try {
                            cost = Float.parseFloat( s.toString() );
                        }
                        catch (NumberFormatException e ) {
                            cost = 0f;
                        }
                    }

                    purchaseInfo.setCost( cost );
                }
            };

            editPurchaseDate.setOnClickListener( showDatePickerOnClick );
            purchaseDateLayout.setStartIconOnClickListener( showDatePickerOnClick );
            editPurchaseCost.addTextChangedListener( fieldTextWatchers[0]);

            if( purchaseInfo != null ){
               editPurchaseDate.setText( dateFormat.format( purchaseInfo.getPurchaseDate() ) );
               editPurchaseCost.setText( String.valueOf( purchaseInfo.getCost() ) );
            }
        });
    }
}