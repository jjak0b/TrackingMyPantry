package com.jjak0b.android.trackingmypantry.ui.util;

import android.content.Context;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jjak0b.android.trackingmypantry.util.Callback;

public class QuantityPickerBuilder extends MaterialAlertDialogBuilder {

    private NumberPicker numberPicker;

    public QuantityPickerBuilder(@NonNull Context context) {
        super(context);
        numberPicker = new NumberPicker(context);
        setView(numberPicker);
    }

    @NonNull
    public QuantityPickerBuilder setMin(int min) {
        numberPicker.setMinValue(min);
        return this;
    }

    @NonNull
    public QuantityPickerBuilder setMax(int max) {
        numberPicker.setMaxValue(max);
        return this;
    }

    @NonNull
    public QuantityPickerBuilder setPositiveButton(int textId, @Nullable Callback<Integer> callback ) {
        super.setPositiveButton(textId, (dialogInterface, i) -> {
            if( callback != null ) callback.apply(numberPicker.getValue());
        });
        return this;
    }
}
