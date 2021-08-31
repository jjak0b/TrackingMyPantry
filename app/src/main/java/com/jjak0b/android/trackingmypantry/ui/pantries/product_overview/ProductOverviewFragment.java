package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjak0b.android.trackingmypantry.R;

public class ProductOverviewFragment extends Fragment {

    private ProductOverviewViewModel mSharedViewModelForNav;
    private ProductOverviewViewModel mSharedViewModel;

    private final static String TAG = "ProductOverview";
    public static ProductOverviewFragment newInstance() {
        return new ProductOverviewFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.product_oveview_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottom_nav);
        final FloatingActionButton fab_edit = view.findViewById(R.id.fab_edit);

        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                .findFragmentById(R.id.product_navigation_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());

        fab_edit.setOnClickListener( v -> Navigation.findNavController(view)
                .navigate(ProductOverviewFragmentDirections.actionEditProductDetails())
        );

        mSharedViewModelForNav = new ViewModelProvider(navHostFragment).get(ProductOverviewViewModel.class);
        mSharedViewModel = new ViewModelProvider(requireParentFragment()).get(ProductOverviewViewModel.class);

        mSharedViewModel.getProduct().observe(getViewLifecycleOwner(), mSharedViewModelForNav::setProduct );

        String productID = ProductOverviewFragmentArgs.fromBundle(getArguments())
                .getProductID();

        Log.e(TAG, "setting ProductID " + productID);
        mSharedViewModel.setProductID(productID);

    }

}