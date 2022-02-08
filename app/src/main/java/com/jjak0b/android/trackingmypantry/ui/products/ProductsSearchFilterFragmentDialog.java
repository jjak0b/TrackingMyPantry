package com.jjak0b.android.trackingmypantry.ui.products;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.ui.util.MultiSelectItemsFragmentDialog;
import com.jjak0b.android.trackingmypantry.ui.util.MultiSelectItemsViewModel;

import java.util.List;

public class ProductsSearchFilterFragmentDialog extends MultiSelectItemsFragmentDialog<ProductTag> {

    private ProductsSearchFilterViewModel mSearchViewModel;
    private MultiSelectProductsTagsViewModel mViewModel;
    private AlertDialog dialog;
    private static final String HEADER_TAG = "header";

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
        this.dialog = (AlertDialog) dialog;

        TextView header = new TextView(requireContext());
        header.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );
        header.setGravity(Gravity.CENTER);
        header.setId(View.generateViewId());
        header.setTag(HEADER_TAG);

        this.dialog.getListView().addHeaderView(header, HEADER_TAG, false);
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

        TextView header = getHeaderView();

        mViewModel.getAllItems().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    header.setText(R.string.loading);
                    header.setVisibility(View.VISIBLE);
                    break;
                default:
                    List<ProductTag> items = resource.getData();

                    if( header != null ) {
                        if( items != null && items.isEmpty() ) {
                            header.setText(R.string.filter_no_options_available);
                            header.setVisibility(View.VISIBLE);
                        }
                        else {
                            header.setText(null);
                            header.setVisibility(View.GONE);
                        }
                    }
                    break;
            }
        });

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


    private TextView getHeaderView() {
        return this.dialog.getListView().findViewWithTag(HEADER_TAG);
    }
}