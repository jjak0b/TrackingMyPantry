package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductInstanceDetailsFragment extends Fragment {

    final static String TAG = ProductInstanceDetailsFragment.class.getName();
    protected ProductInstanceDetailsViewModel mViewModel;

    public ProductInstanceDetailsFragment() {
        // Required empty public constructor
    }

    protected ProductInstanceDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(ProductInstanceDetailsViewModel.class);
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

        TextInputLayout expireDateInputLayout = view.findViewById( R.id.product_expire_date );
        TextInputEditText expireDateInput = view.findViewById(R.id.editTextDate_register_product_expire_date);
        TextInputEditText quantityInput = view.findViewById(R.id.input_product_quantity);
        MaterialAutoCompleteTextView pantryAutoCompleteSelector = view.findViewById( R.id.product_pantry_selector);
        TextInputLayout pantryInputLayout = view.findViewById( R.id.product_pantry );

        Calendar expireDateCalendar = Calendar.getInstance();
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat( getContext() );
        ArrayAdapter<Pantry> pantriesAdapter =  new ArrayAdapter<>( requireContext(), android.R.layout.simple_spinner_dropdown_item);
        pantryAutoCompleteSelector.setAdapter(  pantriesAdapter );

        quantityInput.setOnFocusChangeListener((v, hasFocus) -> {
            if( !hasFocus ){
                int c;
                try {
                    c = Integer.parseInt(quantityInput.getText().toString());
                }
                catch (NumberFormatException e ){
                    c = -1;
                }


                if( c <= 0 ){
                    c = 1;
                }
                mViewModel.setQuantity(c);
                quantityInput.setText( String.valueOf( c ) );
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

                            mViewModel.setExpireDate(date);
                        }
                    },
                    expireDateCalendar.get(Calendar.YEAR),
                    expireDateCalendar.get(Calendar.MONTH),
                    expireDateCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        };
        expireDateInputLayout.setStartIconOnClickListener(showDatePickerOnClick);
        expireDateInput.setOnClickListener(showDatePickerOnClick);

        pantryAutoCompleteSelector.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pantry selectedPantry = pantriesAdapter.getItem( position );
                Log.d( TAG, "selected pantry: " + selectedPantry );
                mViewModel.setPantry(selectedPantry);
            }
        });

        mViewModel.getAvailablePantries().observe( getViewLifecycleOwner(), pantries -> {
            pantriesAdapter.clear();
            if( pantries != null )
                pantriesAdapter.addAll( pantries );
            // needs this to show eventually autocomplete spinner
            // because we need at least a character even if completion threshold = 0
            pantryAutoCompleteSelector.setText(pantryAutoCompleteSelector.getText());
        });

        mViewModel.getPantry().observe( getViewLifecycleOwner(), pantry -> {
            Log.d( TAG, "updated pantry to: " + pantry );
            pantryAutoCompleteSelector.setText( pantry != null ? pantry.toString() : null );

           pantryAutoCompleteSelector.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if( !hasFocus ){

                        String fieldValue = pantryAutoCompleteSelector.getText().toString();
                        boolean hasBeenChanged = true;
                        if( pantry != null ){
                            hasBeenChanged = !Objects.equals( pantry.toString(), fieldValue );
                        }
                        Log.d( TAG, "has been changed:" + hasBeenChanged);
                        if( hasBeenChanged ){
                            if( pantryAutoCompleteSelector.getText().length() > 0 )
                                mViewModel.setPantry( Pantry.creteDummy( pantryAutoCompleteSelector.getText().toString() ) );
                            else
                                mViewModel.setPantry( null );
                        }
                    }
                }
            });
        });

        mViewModel.getQuantity().observe(getViewLifecycleOwner(), quantity -> {
            String strValue = String.valueOf(quantity);
            quantityInput.setText(strValue);
            quantityInput.setSelection(strValue.length());
        });

        mViewModel.getExpireDate().observe(getViewLifecycleOwner(), date -> {
            expireDateInput.setText( dateFormat.format( date ) );

            // dialog will start with this date set
            expireDateCalendar.setTime(date);
        });

        mViewModel.onSave().observe(getViewLifecycleOwner(), shouldSave -> {
            if( !shouldSave ) return;

            // close any open keyboard
            InputUtil.hideKeyboard(requireActivity());
        });
    }
}