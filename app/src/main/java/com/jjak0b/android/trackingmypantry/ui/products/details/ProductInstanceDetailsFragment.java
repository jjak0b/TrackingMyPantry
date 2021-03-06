package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductInstanceDetailsFragment extends Fragment {

    final static String TAG = ProductInstanceDetailsFragment.class.getName();
    protected ProductInstanceDetailsViewModel mViewModel;

    public ProductInstanceDetailsFragment() {
        // Required empty public constructor
    }

    @NonNull
    public ProductInstanceDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(ProductInstanceDetailsViewModel.class);
    }

    @NonNull
    private ProductInstanceDetailsViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = initViewModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_section_product_instance_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputLayout expireDateInputLayout = view.findViewById( R.id.productExpireDateInputLayout);
        TextInputEditText expireDateInput = view.findViewById(R.id.editTextDate_register_product_expire_date);
        TextInputLayout quantityInputLayout = view.findViewById(R.id.productQuantityInputLayout);
        TextInputEditText quantityInput = view.findViewById(R.id.input_product_quantity);
        MaterialAutoCompleteTextView pantryAutoCompleteSelector = view.findViewById( R.id.product_pantry_selector);
        TextInputLayout pantryInputLayout = view.findViewById( R.id.productPantryInputLayout);
        Calendar expireDateCalendar = Calendar.getInstance();
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat( getContext() );
        ArrayAdapter<Pantry> pantriesAdapter = new ArrayAdapter<>( requireContext(), android.R.layout.simple_spinner_dropdown_item);
        pantryAutoCompleteSelector.setAdapter( pantriesAdapter );

        quantityInput.addTextChangedListener(new InputUtil.FieldTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                getViewModel().setQuantity(s.toString());
            }
        });

        View.OnClickListener showDatePickerOnClick = v -> {
            // date picker dialog
            new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view1, int year, int monthOfYear, int dayOfMonth) {
                            expireDateCalendar.set(Calendar.YEAR, year);
                            expireDateCalendar.set(Calendar.MONTH, monthOfYear);
                            expireDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            expireDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            expireDateCalendar.set(Calendar.MINUTE, 0);
                            expireDateCalendar.set(Calendar.SECOND, 0);
                            expireDateCalendar.set(Calendar.MILLISECOND, 0);
                            Date date = expireDateCalendar.getTime();

                            getViewModel().setExpireDate(date);
                        }
                    },
                    expireDateCalendar.get(Calendar.YEAR),
                    expireDateCalendar.get(Calendar.MONTH),
                    expireDateCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        };
        expireDateInputLayout.setStartIconOnClickListener(showDatePickerOnClick);
        expireDateInput.setOnClickListener(showDatePickerOnClick);

        pantryAutoCompleteSelector.addTextChangedListener(new InputUtil.FieldTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { getViewModel().setPantry(s.toString()); }
        });

        pantryAutoCompleteSelector.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pantry selectedPantry = pantriesAdapter.getItem( position );
                Log.d( TAG, "selected pantry: " + selectedPantry );
                getViewModel().setPantry(selectedPantry);
            }
        });

        getViewModel().getAvailablePantries().observe( getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    if( resource.getData() != null )
                        pantriesAdapter.addAll( resource.getData() );
                    // needs this to show eventually autocomplete spinner
                    // because we need at least a character even if completion threshold = 0
                    pantryAutoCompleteSelector.setText(pantryAutoCompleteSelector.getText());
                    break;
                default:
                    pantriesAdapter.clear();
                    break;
            }
        });

        getViewModel().getPantry().observe( getViewLifecycleOwner(), resource -> {
            Pantry pantry = resource.getData();
            InputUtil.setText(pantryAutoCompleteSelector, pantry != null ? pantry.toString() : null );

            switch (resource.getStatus()) {
                case LOADING:
                    pantryInputLayout.setError(null);
                    break;
                case SUCCESS:
                    pantryInputLayout.setError(null);
                    // unset pantry and set as custom after edited the text
                    break;
                case ERROR:
                    pantryInputLayout.setError(resource.getError().getLocalizedMessage());
                    break;
            }
        });

        getViewModel().getQuantity().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    quantityInputLayout.setError(null);
                    InputUtil.setText(quantityInput, String.valueOf(resource.getData()));
                    break;
                case ERROR:
                    quantityInputLayout.setError(resource.getError().getLocalizedMessage());
                    break;
                default:
                    break;
            }
        });

        getViewModel().getExpireDate().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    expireDateInputLayout.setError(null);
                    expireDateInput.setText( dateFormat.format( resource.getData() ) );

                    // dialog will start with this date set
                    expireDateCalendar.setTime(resource.getData());
                    break;
                case ERROR:
                    expireDateInputLayout.setError(resource.getError().getLocalizedMessage());
                    break;
                default:
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