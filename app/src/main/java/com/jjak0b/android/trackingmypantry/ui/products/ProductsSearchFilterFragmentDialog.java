package com.jjak0b.android.trackingmypantry.ui.products;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.util.TagsPicker.TagsPickerDialog;

public class ProductsSearchFilterFragmentDialog extends TagsPickerDialog {

    public static ProductsSearchFilterFragmentDialog newInstance() {
        return new ProductsSearchFilterFragmentDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.setup_filter_options);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_products_search_filter, container);
    }

}