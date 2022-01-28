package com.jjak0b.android.trackingmypantry.ui.products.product_overview;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.ui.register_product.SharedProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;

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
        setHasOptionsMenu(true);
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
        final FloatingActionButton fab_add = view.findViewById(R.id.fab_add);
        final ImageView appBarImage = view.findViewById(R.id.app_bar_image);

        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                .findFragmentById(R.id.product_navigation_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());

        fab_add.setOnClickListener( v -> onActionAdd() );

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

        LiveData<Resource<UserProduct>> source = mViewModel.get(productID);
        mSharedViewModel.setItemSource(source);
        mSharedViewModelForNav.setItemSource(source);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.product_overview_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                onActionRemove();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onActionAdd() {
        String productID = ProductOverviewFragmentArgs.fromBundle(getArguments())
                .getProductID();
        Navigation.findNavController(requireView())
                .navigate(ProductOverviewFragmentDirections.actionAddProduct(productID));
    }

    private void onActionRemove() {
        LiveData<Resource<UserProduct>> mCurrentProduct = mSharedViewModel.getItem();
        NavController navController = Navigation.findNavController(requireView());
        new MaterialAlertDialogBuilder(requireContext())
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {

                    LiveData<Resource<UserProduct>> onRemove = Transformations.forwardOnce( mCurrentProduct, resource -> {
                        return mViewModel.remove(resource.getData());
                    });

                    onRemove.observe(getViewLifecycleOwner(), resource -> {
                        if( resource.getStatus() != Status.LOADING ) {
                            onRemove.removeObservers(getViewLifecycleOwner());

                            // close dialog
                            dialogInterface.dismiss();
                            navController.popBackStack();

                            // notify error to user
                            if( resource.getStatus() == Status.ERROR) {
                                new MaterialAlertDialogBuilder(requireContext())
                                        .setMessage(ErrorsUtils.getErrorMessage(requireContext(), resource.getError(), TAG) )
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show();
                            }
                        }
                    });
                })
                .setNegativeButton(android.R.string.no, null )
                .setTitle(R.string.option_remove_entry)
                .setMessage(R.string.product_action_remove_description)
                .show();
    }
}