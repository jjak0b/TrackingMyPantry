package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
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
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class SectionProductInstanceDetailsFragment extends Fragment {

    final static String TAG = SectionProductInstanceDetailsFragment.class.getName();
    private RegisterProductViewModel mViewModel;

    public SectionProductInstanceDetailsFragment() {
        // Required empty public constructor
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

        mViewModel = new ViewModelProvider(requireActivity())
                .get(RegisterProductViewModel.class);

        TextInputLayout expireDateInputLayout = view.findViewById( R.id.product_expire_date );
        TextInputEditText expireDateInput = view.findViewById(R.id.editTextDate_register_product_expire_date);
        TextInputEditText quantityInput = view.findViewById(R.id.input_product_quantity);
        MaterialAutoCompleteTextView pantryAutoCompleteSelector = view.findViewById( R.id.product_pantry_selector);
        TextInputLayout pantryInputLayout = view.findViewById( R.id.product_pantry );
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat( getContext() );
        ArrayAdapter<Pantry> pantriesAdapter =  new ArrayAdapter<>( requireContext(), android.R.layout.simple_spinner_dropdown_item);
        pantryAutoCompleteSelector.setAdapter(  pantriesAdapter );

        mViewModel.getAvailablePantries().observe( getViewLifecycleOwner(), pantries -> {
            pantriesAdapter.clear();
            if( pantries != null )
                pantriesAdapter.addAll( pantries );
        });

        pantryAutoCompleteSelector.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pantry selectedPantry = pantriesAdapter.getItem( position );
                Log.d( TAG, "selected pantry: " + selectedPantry );
                if( selectedPantry != null ) {
                    mViewModel.setPantry(selectedPantry);
                }
            }
        });

        mViewModel.getPantry().observe( getViewLifecycleOwner(), pantry -> {
            Log.d( TAG, "updated pantry to: " + pantry );
            if( pantry != null ) {
                pantryAutoCompleteSelector.setText( pantry.toString() );
            }
            else{
                pantryAutoCompleteSelector.setText(null);
            }

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
                            mViewModel.setPantry( Pantry.creteDummy( pantryAutoCompleteSelector.getText().toString() ) );
                        }
                    }
                }
            });

            pantryAutoCompleteSelector.addTextChangedListener(new InputUtil.FieldTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        });

        mViewModel.getProductInstance().observe( getViewLifecycleOwner(), productInstance -> {
            if( productInstance == null ){
                // set default values in fields
                mViewModel.resetProductInstance();
                return;
            }

            Calendar calendar = Calendar.getInstance();

            quantityInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
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

                        productInstance.setQuantity( c );
                        quantityInput.setText( String.valueOf( c ) );
                }
            }});

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

                                    productInstance.setExpiryDate(date);
                                    expireDateInput.setText(dateFormat.format(date));
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    picker.show();
                }
            };
            expireDateInputLayout.setEndIconOnClickListener(showDatePickerOnClick);
            expireDateInput.setOnClickListener(showDatePickerOnClick);

            if( productInstance != null ){
                expireDateInput.setText( dateFormat.format( productInstance.getExpiryDate() ) );
                quantityInput.setText( String.valueOf( productInstance.getQuantity() ));
            }
        });
    }
}