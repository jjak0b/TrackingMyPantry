package com.jjak0b.android.trackingmypantry.ui.products.product_overview;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.ui.register_product.SharedProductViewModel;

public class ProductOverviewFragment extends Fragment {

    private ProductOverviewViewModel mViewModel;
    private SharedProductViewModel mSharedViewModelForNav;
    private SharedProductViewModel mSharedViewModel;

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
        final ImageView appBarImage = view.findViewById(R.id.app_bar_image);

        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                .findFragmentById(R.id.product_navigation_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());

        fab_edit.setOnClickListener( v -> Navigation.findNavController(view)
                .navigate(ProductOverviewFragmentDirections.actionEditProductDetails())
        );

        mViewModel = new ViewModelProvider(this).get(ProductOverviewViewModel.class);
        mSharedViewModelForNav = new ViewModelProvider(navHostFragment).get(SharedProductViewModel.class);
        // used for Edit product
        mSharedViewModel = new ViewModelProvider(requireParentFragment()).get(SharedProductViewModel.class);

        mSharedViewModel.getItem().observe(getViewLifecycleOwner(), resource -> {

            String imgURI = resource.getData() != null ? resource.getData().getImg() : null;

            Glide.with(view)
                    .load(imgURI)
                    .fitCenter()
                    .placeholder(R.drawable.loading_spinner)
                    .fallback(R.drawable.ic_baseline_product_placeholder)
                    .into(appBarImage);

        });

        String productID = ProductOverviewFragmentArgs.fromBundle(getArguments())
                .getProductID();

        Log.e(TAG, "setting ProductID " + productID);

        LiveData<Resource<Product>> source = mViewModel.get(productID);
        mSharedViewModel.setItemSource(source);
        mSharedViewModelForNav.setItemSource(source);

    }

}