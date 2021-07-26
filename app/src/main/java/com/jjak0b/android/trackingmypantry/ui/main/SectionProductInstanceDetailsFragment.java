package com.jjak0b.android.trackingmypantry.ui.main;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;

import com.google.android.material.textfield.TextInputEditText;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class SectionProductInstanceDetailsFragment extends Fragment {

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
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(requireActivity())
                .get(RegisterProductViewModel.class);

        TextInputEditText expireDateInput = view.findViewById(R.id.editTextDate_register_product_expire_date);
        TextInputEditText quantityInput = view.findViewById(R.id.input_product_quantity);
        AutoCompleteTextView pantryAutoCompleteSelector = view.findViewById( R.id.product_pantry_selector);

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat( getContext() );
        ArrayAdapter<Pantry> pantriesAdapter =  new ArrayAdapter<>( requireContext(), R.layout.support_simple_spinner_dropdown_item);
        pantryAutoCompleteSelector.setAdapter(  pantriesAdapter );

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

                    if( c > 0 ){
                        mViewModel.setArticlesCount( c );
                    }
                    else{
                        mViewModel.setArticlesCount( 1 );
                        quantityInput.setText( String.valueOf( 1 ) );
                    }
                }
            }
        });

        mViewModel.getArticlesCount().observe( getViewLifecycleOwner(), count -> {
            String t = String.valueOf( count );
            quantityInput.setText( t );
            quantityInput.setSelection( t.length() );
        });
/*
        mViewModel.getAvailablePantries().observe( getViewLifecycleOwner(), pantries -> {
            pantriesAdapter.clear();
            if( pantries != null )
                pantriesAdapter.addAll( pantries );
        });
*/
        mViewModel.getProductInstance().observe( getViewLifecycleOwner(), productInstance -> {
            if( productInstance == null ){
                // set default values in fields
                mViewModel.resetProductInstance();
                return;
            }

            Calendar calendar = Calendar.getInstance();

            pantryAutoCompleteSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Pantry pantry = pantriesAdapter.getItem( position );
                    productInstance.setPantryId(pantry.getId());
                }
            });

            expireDateInput.setOnClickListener(new View.OnClickListener() {
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
            });

            if( productInstance != null ){
                if( productInstance.getPantryId() > 0 ){
                    int position = pantriesAdapter.getPosition( Pantry.creteDummy( productInstance.getPantryId() ) );
                    pantryAutoCompleteSelector.setSelection( position );
                }
                expireDateInput.setText( dateFormat.format( productInstance.getExpiryDate() ) );
            }
        });
    }
}