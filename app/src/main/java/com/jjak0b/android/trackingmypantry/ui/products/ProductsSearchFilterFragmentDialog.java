package com.jjak0b.android.trackingmypantry.ui.products;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;

import java.util.ArrayList;
import java.util.Objects;

public class ProductsSearchFilterFragmentDialog extends DialogFragment {

    private ProductsSearchFilterViewModel mViewModel;
    private ArrayAdapter<ProductTag> tagSuggestionsAdapter;
    public static ProductsSearchFilterFragmentDialog newInstance() {
        return new ProductsSearchFilterFragmentDialog();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tagSuggestionsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_multiple_choice);
        mViewModel = new ViewModelProvider(requireParentFragment()).get(ProductsSearchFilterViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog newDialog = new MaterialAlertDialogBuilder(requireContext())
                .setAdapter(tagSuggestionsAdapter, null)
                .setTitle(R.string.setup_filter_options)
                .setPositiveButton(android.R.string.ok, onSubmit )
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        newDialog.getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        return newDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_products_search_filter, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final AlertDialog dialog = (AlertDialog) getDialog();

        mViewModel.getSearchTagsSuggestions().observe(getViewLifecycleOwner(), tags -> {
            tagSuggestionsAdapter.clear();
            if( tags != null )
                tagSuggestionsAdapter.addAll(tags);
            tagSuggestionsAdapter.notifyDataSetChanged();
        });

        mViewModel.getSearchTags().observe(getViewLifecycleOwner(), tags -> {
            boolean checked;
            for (int i = 0; i < tagSuggestionsAdapter.getCount(); i++) {
                checked = false;
                if( tags != null ) {
                    for (ProductTag tag : tags) {
                        if (Objects.equals(tag, tagSuggestionsAdapter.getItem(i))) {
                            checked = true;
                            break;
                        }
                    }
                }
                dialog.getListView().setItemChecked(i, checked);
            }
        });
    }

    DialogInterface.OnClickListener onSubmit = (dialog, which) -> {
        int checkedItemCount = ((AlertDialog)dialog).getListView().getCheckedItemCount();
        SparseBooleanArray checkedItems = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
        ArrayList<ProductTag> checkedTags = new ArrayList<>(checkedItemCount);
        if( checkedItems != null ) {
            for (int i = 0; i < checkedItems.size(); i++) {
                int tagPosition = checkedItems.keyAt(i);
                boolean isChecked = checkedItems.get(tagPosition);
                if (isChecked) {
                    checkedTags.add(tagSuggestionsAdapter.getItem(tagPosition));
                }
            }
        }
        mViewModel.setSearchTags(checkedTags);
    };
}