package com.jjak0b.android.trackingmypantry.ui.main;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.jjak0b.android.trackingmypantry.R;

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


        TextInputEditText expireDateInput = (TextInputEditText) view.findViewById(R.id.editTextDate_register_product_expire_date);
        // Spinner pantrySelector = (Spinner) itemView.findViewById(R.id.spinner_pantry_selector);

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat( getContext() );

        mViewModel = new ViewModelProvider(requireActivity())
                .get(RegisterProductViewModel.class);

        Calendar cldr = Calendar.getInstance();
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {


                                cldr.set(Calendar.YEAR, year);
                                cldr.set(Calendar.MONTH, monthOfYear);
                                cldr.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                Date date = cldr.getTime();

                                // item.setExpiryDate( date );
                                expireDateInput.setText( dateFormat.format( date ) );
                            }
                        },
                        cldr.get(Calendar.YEAR),
                        cldr.get(Calendar.MONTH),
                        cldr.get(Calendar.DAY_OF_MONTH)
                );
                picker.show();
            }
        };

        expireDateInput.setOnClickListener( onClickListener );
        expireDateInput.setText( dateFormat.format( cldr.getTime() ) );
    }
}