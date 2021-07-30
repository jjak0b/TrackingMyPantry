package com.jjak0b.android.trackingmypantry.ui.pantries;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjak0b.android.trackingmypantry.R;

public class ProductInstanceGroupListFragment extends Fragment {

    private ProductInstanceGroupListViewModel mViewModel;

    public static ProductInstanceGroupListFragment newInstance() {
        return new ProductInstanceGroupListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.product_instance_group_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProductInstanceGroupListViewModel.class);
        // TODO: Use the ViewModel
    }

}