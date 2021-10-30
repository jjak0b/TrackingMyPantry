package com.jjak0b.android.trackingmypantry.ui.pantries;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.ui.util.ChipTagUtil;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;

public class ProductsSearchFilterFragmentDialog extends BottomSheetDialogFragment {

    private ProductsSearchFilterViewModel mViewModel;

    public static ProductsSearchFilterFragmentDialog newInstance() {
        return new ProductsSearchFilterFragmentDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProductsSearchFilterViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.products_search_filter_fragment_dialog_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText searchQueryField = view.findViewById(R.id.searchText);
        NachoTextView searchTagsField = view.findViewById(R.id.searchTags);
        Button btnReset = view.findViewById(R.id.btnReset);
        Button btnSubmit = view.findViewById(R.id.btnSearch);

        btnReset.setOnClickListener(v -> mViewModel.reset());
        btnSubmit.setOnClickListener(v -> mViewModel.search());

        searchQueryField.addTextChangedListener(new InputUtil.FieldTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setSearchQuery(s.toString());
            }
        });
        searchTagsField.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR );
        ArrayAdapter<ProductTag> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line
        );
        searchTagsField.setAdapter( adapter );
        searchTagsField.setOnFocusChangeListener( (v, hasFocus) -> {
            /*if( !hasFocus ){
                mViewModel.setSearchTags(searchTagsField.getChipValues());
            }*/
        });

        mViewModel.getSearchQuery().observe(getViewLifecycleOwner(), s -> {
            searchQueryField.setText(s);
            if( s != null)
                searchQueryField.setSelection(s.length());
        });

        mViewModel.getSearchTags().observe(getViewLifecycleOwner(), tags -> {
            searchTagsField.setText(tags);
        });
        mViewModel.getSuggestions().observe( getViewLifecycleOwner(), suggestions -> {
            adapter.clear();
            adapter.addAll( suggestions );
        });
    }
}