package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.app.Activity;
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
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;
import com.schibstedspain.leku.LekuPoi;
import com.schibstedspain.leku.LocationPickerActivity;

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

        locationPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            int resultCode = result.getResultCode();
            Intent data = result.getData();
            /*if (resultCode == Activity.RESULT_OK && data != null) {
                Log.d("RESULT****", "OK")
                if (requestCode == 1) {
                    int latitude = data.getDoubleExtra( LocationPickerActivity.LATITUDE, 0.0)
                    Log.d("LATITUDE****", latitude.toString())
                    val longitude = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0.0)
                    Log.d("LONGITUDE****", longitude.toString())
                    val address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS)
                    Log.d("ADDRESS****", address.toString())
                    val postalcode = data.getStringExtra(LocationPickerActivity.ZIPCODE)
                    Log.d("POSTALCODE****", postalcode.toString())
                    val bundle = data.getBundleExtra(LocationPickerActivity.TRANSITION_BUNDLE)
                    Log.d("BUNDLE TEXT****", bundle.getString("test"))
                    val fullAddress = data.getParcelableExtra<Address>(ADDRESS)
                    if (fullAddress != null) {
                        Log.d("FULL ADDRESS****", fullAddress.toString())
                    }
                    val timeZoneId = data.getStringExtra(LocationPickerActivity.TIME_ZONE_ID)
                    Log.d("TIME ZONE ID****", timeZoneId)
                    val timeZoneDisplayName = data.getStringExtra(LocationPickerActivity.TIME_ZONE_DISPLAY_NAME)
                    Log.d("TIME ZONE NAME****", timeZoneDisplayName)
                } else if (requestCode == 2) {
                    val latitude = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0.0)
                    Log.d("LATITUDE****", latitude.toString())
                    val longitude = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0.0)
                    Log.d("LONGITUDE****", longitude.toString())
                    val address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS)
                    Log.d("ADDRESS****", address.toString())
                    val lekuPoi = data.getParcelableExtra<LekuPoi >(LocationPickerActivity.LEKU_POI)
                            Log.d("LekuPoi****", lekuPoi.toString())
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("RESULT****", "CANCELLED")
            }*/
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
                try {
                    ApplicationInfo appInfo = requireContext().getPackageManager()
                            .getApplicationInfo(requireContext().getPackageName(), PackageManager.GET_META_DATA );
                    String apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
                    Intent locationPickerIntent = new LocationPickerActivity.Builder()
                            // .withLocation(41.4036299, 2.1743558)
                            .withGeolocApiKey(apiKey)
                            .withGooglePlacesApiKey(apiKey)
                            // .withSearchZone("es_ES")
                            // .withSearchZone(SearchZoneRect(LatLng(26.525467, -18.910366), LatLng(43.906271, 5.394197)))
                            .withDefaultLocaleSearchZone()
                            .shouldReturnOkOnBackPressed()
                            .withStreetHidden()
                            .withCityHidden()
                            .withZipCodeHidden()
                            .withSatelliteViewHidden()
                            .withGoogleTimeZoneEnabled()
                            .withVoiceSearchHidden()
                            .withUnnamedRoadHidden()
                            .build(requireContext());

                    locationPickerLauncher.launch(locationPickerIntent);
                }
                catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
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
            purchaseDateLayout.setEndIconOnClickListener( showDatePickerOnClick );

            editPurchaseLocation.setOnClickListener( showLocationPickerOnClick );
            purchaseDateLayout.setEndIconOnClickListener( showLocationPickerOnClick );

            editPurchaseCost.addTextChangedListener( fieldTextWatchers[0]);

            if( purchaseInfo != null ){
               editPurchaseDate.setText( dateFormat.format( purchaseInfo.getPurchaseDate() ) );
               editPurchaseCost.setText( String.valueOf( purchaseInfo.getCost() ) );
            }
        });
    }
}