package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries;

import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.ProductOverviewViewModel;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups.ProductsGroupsBrowserViewModel;

public class PantriesBrowserFragment extends Fragment {

    private PantriesBrowserViewModel mViewModel;
    private ProductOverviewViewModel mProductViewModel;
    private PantryListAdapter listAdapter;
    private ProductsGroupsBrowserViewModel mProductsGroupsBrowserViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PantriesBrowserViewModel.class);
        mProductViewModel = new ViewModelProvider(requireParentFragment()).get(ProductOverviewViewModel.class);
        mProductsGroupsBrowserViewModel = new ViewModelProvider(requireParentFragment()).get(ProductsGroupsBrowserViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantries_browser, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ProgressBar loadingBar = (ProgressBar) view.findViewById(R.id.pantriesLoadingBar);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        final TextView listInfo = (TextView) view.findViewById( R.id.listInfo );

        listAdapter = new PantryListAdapter( new PantryListAdapter.ProductDiff(), pantryInteractionsListener );
        loadingBar.setVisibility( View.VISIBLE );
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter( listAdapter );

        mProductViewModel.getProduct().observe(getViewLifecycleOwner(), productWithTags -> {
            Log.e( "MyPantries", "setting productID" );
            if( productWithTags != null ){
                mViewModel.setProductID(productWithTags.product.getId());
            }
        });

        mViewModel.getList().observe( getViewLifecycleOwner(), pantriesWGroups -> {
            Log.e( "MyPantries", "submitting new list of " + pantriesWGroups.size() + "elements from " +  this.toString() + " " );
            if( pantriesWGroups.isEmpty() ){
                listInfo.setVisibility( View.VISIBLE );
            }
            else {
                listInfo.setVisibility( View.GONE );

            }
            loadingBar.setVisibility( View.VISIBLE );
            listAdapter.submitList( pantriesWGroups );
            loadingBar.setVisibility( View.GONE );
        });

        // update groups browser on current pantry update
        mViewModel.getCurrentPantry().observe(getViewLifecycleOwner(), pantryWithProductInstanceGroups -> {
            if( pantryWithProductInstanceGroups == null ){
                mProductsGroupsBrowserViewModel.setPantry(null);
                mProductsGroupsBrowserViewModel.setGroups(null);
            }
            else {
                mProductsGroupsBrowserViewModel.setPantry(pantryWithProductInstanceGroups.pantry);
                mProductsGroupsBrowserViewModel.setGroups(pantryWithProductInstanceGroups.instances);
            }
        });
    }

    final PantryInteractionsListener pantryInteractionsListener = new PantryInteractionsListener() {
        @Override
        public void onItemClicked(int pantryPosition, View pantryView, PantryWithProductInstanceGroups item) {
            NavController navController = Navigation.findNavController(requireView());
            NavDirections direction = PantriesBrowserFragmentDirections.actionShowPantryContent();
            if( navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getAction(direction.getActionId()) != null ){
                mViewModel.setCurrentPantry(item);
                navController.navigate(direction);
            }
        }

        @Override
        public void onItemLongClicked(int pantryPosition, View pantryView, PantryWithProductInstanceGroups item) {

        }
    };

}