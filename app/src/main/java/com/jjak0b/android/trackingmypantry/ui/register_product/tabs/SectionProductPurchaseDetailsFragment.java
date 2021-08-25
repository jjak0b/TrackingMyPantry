package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.register_product.RegisterProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class SectionProductPurchaseDetailsFragment extends Fragment {

    private RegisterProductViewModel mViewModel;

    public SectionProductPurchaseDetailsFragment() {
        // Required empty public constructor
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
            editPurchaseCost.addTextChangedListener( fieldTextWatchers[0]);

            if( purchaseInfo != null ){
               editPurchaseDate.setText( dateFormat.format( purchaseInfo.getPurchaseDate() ) );
               editPurchaseCost.setText( String.valueOf( purchaseInfo.getCost() ) );
            }
        });
    }
}