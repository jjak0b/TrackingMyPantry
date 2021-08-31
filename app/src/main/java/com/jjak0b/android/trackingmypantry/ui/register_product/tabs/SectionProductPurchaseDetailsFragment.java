package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;


import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.maps.PlacesPluginActivity;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.location.DefaultLocationProvider;
import com.mapbox.search.ui.view.SearchBottomSheetView;
import com.mapbox.search.ui.view.SearchMode;
import com.mapbox.search.ui.view.favorite.FavoriteTemplate;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

        locationPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            int resultCode = result.getResultCode();
            Intent data = result.getData();
            if (resultCode == Activity.RESULT_OK && data != null) {
                Log.d("RESULT****", "OK");
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        Log.e("RESULT", key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                    }
                }
                CarmenFeature carmenFeature = PlacesPluginActivity.getPlace(data);

                Log.d("RESULT", carmenFeature.toJson());

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("RESULT****", "CANCELLED");
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

        mViewModel = new ViewModelProvider(requireActivity())
                .get(RegisterProductViewModel.class);

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat( getActivity() );

        TextInputLayout purchaseLocationLayout = view.findViewById( R.id.product_purchase_location);
        TextInputLayout purchaseDateLayout = view.findViewById( R.id.product_purchase_date);
        EditText editPurchaseDate = view.findViewById( R.id.editTextPurchaseDate);
        EditText editPurchaseLocation = view.findViewById( R.id.editTextLocation );
        EditText editPurchaseCost = view.findViewById( R.id.editTextCost );
        InputUtil.FieldTextWatcher[] fieldTextWatchers = new InputUtil.FieldTextWatcher[1];

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

            View.OnClickListener showLocationPickerOnClick = v -> {
                locationPickerLauncher.launch(new Intent(getContext(), PlacesPluginActivity.class));
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
            purchaseDateLayout.setEndIconOnClickListener( showDatePickerOnClick );

            editPurchaseLocation.setOnClickListener( showLocationPickerOnClick );
            purchaseLocationLayout.setEndIconOnClickListener( showLocationPickerOnClick );

            editPurchaseCost.addTextChangedListener( fieldTextWatchers[0]);

            if( purchaseInfo != null ){
               editPurchaseDate.setText( dateFormat.format( purchaseInfo.getPurchaseDate() ) );
               editPurchaseCost.setText( String.valueOf( purchaseInfo.getCost() ) );
            }
        });
    }
}