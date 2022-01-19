package com.jjak0b.android.trackingmypantry.ui.products;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.ui.util.MultiSelectItemsFragmentDialog;
import com.jjak0b.android.trackingmypantry.ui.util.MultiSelectItemsViewModel;

public class ProductsSearchFilterFragmentDialog extends MultiSelectItemsFragmentDialog<ProductTag> {

    private ProductsSearchFilterViewModel mSearchViewModel;
    private MultiSelectProductsTagsViewModel mViewModel;

    public static ProductsSearchFilterFragmentDialog newInstance() {
        return new ProductsSearchFilterFragmentDialog();
    }


    @Override
    public MultiSelectItemsViewModel<ProductTag> initViewModel() {
        mViewModel = new ViewModelProvider(this).get(MultiSelectProductsTagsViewModel.class);
        return mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mSearchViewModel = new ViewModelProvider(requireParentFragment()).get(ProductsSearchFilterViewModel.class);
        super.onCreate(savedInstanceState);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSearchViewModel.getSearchTags().observe(getViewLifecycleOwner(), tags -> {
            mViewModel.setItems(tags);
        });

        mViewModel.getListItems().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    mSearchViewModel.setSearchTags(resource.getData());
                    break;
                case ERROR:
                    break;
            }
        });
    }
}