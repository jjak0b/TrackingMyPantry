package com.jjak0b.android.trackingmypantry.ui.products.details;

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
public class ProductPurchaseDetailsFragment extends Fragment {

    protected ProductPurchaseDetailsViewModel mViewModel;

    private ActivityResultLauncher<Intent> locationPickerLauncher;

    public ProductPurchaseDetailsFragment() {
        // Required empty public constructor
    }

    protected ProductPurchaseDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(ProductPurchaseDetailsViewModel.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = initViewModel();

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
        Calendar calendar = Calendar.getInstance();

        TextInputLayout purchaseLocationLayout = view.findViewById( R.id.product_purchase_location);
        TextInputLayout purchaseDateLayout = view.findViewById( R.id.product_purchase_date);
        EditText editPurchaseDate = view.findViewById( R.id.editTextPurchaseDate);
        EditText editPurchaseLocation = view.findViewById( R.id.editTextLocation );
        EditText editPurchaseCost = view.findViewById( R.id.editTextCost );

        View.OnClickListener showLocationPickerOnClick = v -> {
            locationPickerLauncher.launch(new Intent(getContext(), PlacesPluginActivity.class));
        };

        editPurchaseLocation.setOnClickListener( showLocationPickerOnClick );
        purchaseLocationLayout.setStartIconOnClickListener( showLocationPickerOnClick );

        View.OnClickListener showDatePickerOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // date picker dialog
                 new DatePickerDialog(getContext(),
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

                                mViewModel.setPurchaseDate(date);
                                editPurchaseDate.setText(dateFormat.format(date));
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        };
        editPurchaseDate.setOnClickListener( showDatePickerOnClick );
        purchaseDateLayout.setStartIconOnClickListener( showDatePickerOnClick );

        editPurchaseCost.addTextChangedListener(new InputUtil.FieldTextWatcher() {
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

                mViewModel.setCost( cost );
            }
        });

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

        mViewModel.getPurchaseDate().observe(getViewLifecycleOwner(), date -> {
            editPurchaseDate.setText( dateFormat.format( date ) );
            calendar.setTime(date);
        });

        mViewModel.getCost().observe(getViewLifecycleOwner(), cost -> {
            editPurchaseCost.setText( String.valueOf( cost ) );
        });

        mViewModel.onSave().observe(getViewLifecycleOwner(), shouldSave -> {
            if( !shouldSave ) return;

            // close any open keyboard
            InputUtil.hideKeyboard(requireActivity());
        });
    }
}