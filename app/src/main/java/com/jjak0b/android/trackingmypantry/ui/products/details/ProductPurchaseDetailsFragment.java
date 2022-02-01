package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.ui.maps.PlacesPluginActivity;
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

    @NonNull
    public ProductPurchaseDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(ProductPurchaseDetailsViewModel.class);
    }

    @NonNull
    private ProductPurchaseDetailsViewModel getViewModel() {
        return mViewModel;
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

                getViewModel().setPurchasePlace(place);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                getViewModel().setPurchasePlace(null);
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

        TextInputLayout purchaseLocationLayout = view.findViewById( R.id.productPurchaseLocationInputLayout);
        TextInputLayout purchaseDateLayout = view.findViewById( R.id.productPurchaseDateInputLayout);
        TextInputLayout purchaseCostLayout = view.findViewById( R.id.productCostInputLayout);

        EditText editPurchaseDate = view.findViewById( R.id.editTextPurchaseDate);
        EditText editPurchaseLocation = view.findViewById( R.id.editTextLocation );
        EditText editPurchaseCost = view.findViewById( R.id.editTextCost );

        View.OnClickListener showLocationPickerOnClick = v -> {
            locationPickerLauncher.launch(new Intent(requireContext(), PlacesPluginActivity.class));
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
                                calendar.setTimeInMillis(0);
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, monthOfYear);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                Date date = calendar.getTime();

                                getViewModel().setPurchaseDate(date);
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
                getViewModel().setCost( s.toString() );
            }
        });

        getViewModel().getPurchasePlace().observe( getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case ERROR:
                    Throwable error = resource.getError();
                    if( error != null) {
                        purchaseLocationLayout.setError(error.getLocalizedMessage());
                    }
                    break;
                case SUCCESS:
                    purchaseLocationLayout.setError(null);
                    Place place = resource.getData();
                    if( place != null && place.getName() != null ) {
                        String placeName = place.getName();
                        InputUtil.setText(editPurchaseLocation, placeName);
                    }
                    else {
                        editPurchaseLocation.setText(null);
                    }
                    break;
                default:
                    break;
            }
        });

        getViewModel().getPurchaseDate().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case ERROR:
                    Throwable error = resource.getError();
                    if( error != null) {
                        purchaseDateLayout.setError(error.getLocalizedMessage());
                    }
                    break;
                case SUCCESS:
                    purchaseDateLayout.setError(null);
                    Date date = resource.getData();
                    InputUtil.setText(editPurchaseDate, dateFormat.format( date ));
                    calendar.setTime(date);
                    break;
                default:
                    break;
            }
        });

        getViewModel().getCost().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case ERROR:
                    Throwable error = resource.getError();
                    if( error != null) {
                        purchaseCostLayout.setError(error.getLocalizedMessage());
                    }
                    break;
                case SUCCESS:
                    purchaseCostLayout.setError(null);
                    InputUtil.setText(editPurchaseCost, String.valueOf( resource.getData() ) );
                    break;
            }
        });

    }

    public void setupSave(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getViewModel().onSave().observe(getViewLifecycleOwner(), shouldSave -> {
            if( !shouldSave ) return;

            // close any open keyboard
            InputUtil.hideKeyboard(requireActivity());
        });
    }

    public void setupReset(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }
}