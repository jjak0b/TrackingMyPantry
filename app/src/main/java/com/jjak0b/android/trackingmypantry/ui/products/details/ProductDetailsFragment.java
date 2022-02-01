package com.jjak0b.android.trackingmypantry.ui.products.details;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.ui.util.ChipTagUtil;
import com.jjak0b.android.trackingmypantry.ui.util.FormException;

public class ProductDetailsFragment extends ProductInfoFragment {

    public static ProductDetailsFragment newInstance() {
        return new ProductDetailsFragment();
    }

    @Override
    protected ProductDetailsViewModel initViewModel() {
        return new ViewModelProvider(this).get(ProductDetailsViewModel.class);
    }

    private ProductDetailsViewModel getViewModel() {
        return (ProductDetailsViewModel)mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTags(view, savedInstanceState);

    }

    public void setupTags(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final NachoTextView chipsInput = (NachoTextView) view.findViewById(R.id.chips_input);
        final TextInputLayout tagsInputLayout = view.findViewById(R.id.tagsInputLayout);

        ArrayAdapter<ProductTag> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item);
        chipsInput.setAdapter( adapter );
        chipsInput.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR );
        chipsInput.setOnFocusChangeListener( (v, hasFocus) -> {
            if( !hasFocus ){
                getViewModel().setAssignedTags( ChipTagUtil.newTagsInstanceFromChips( chipsInput.getAllChips() ) );
            }
        });

        getViewModel().getAssignedTags().observe( getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    tagsInputLayout.setError(null);
                    int selection = chipsInput.getSelectionEnd();
                    chipsInput.setTextWithChips( ChipTagUtil.newChipsInstanceFromTags( resource.getData() ) );
                    chipsInput.setSelection(Math.min(selection, chipsInput.getText().length()));
                    break;
                case ERROR:
                    if( resource.getError() instanceof FormException){
                        tagsInputLayout.setError(((FormException) resource.getError()).getLocalizedMessage(requireContext()));
                    }

                    break;
            }
        });

        getViewModel().getSuggestionTags().observe( getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    adapter.addAll( resource.getData() );
                    break;
                default:
                    adapter.clear();
                    break;
            }
        });


        getViewModel().onSave().observe( getViewLifecycleOwner(), shouldSave -> {
            if( !shouldSave ) return;

            // setOnFocusChangeListener of chips tags view is not triggered while clicking on a "save" view
            // so trigger it manually
            if (chipsInput.hasFocus()) {
                chipsInput.clearFocus();
                // mViewModel.setAssignedTags(ChipTagUtil.newTagsInstanceFromChips(chipsInput.getAllChips()));
            }
        });
    }
}
