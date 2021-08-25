package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.jjak0b.android.trackingmypantry.R;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     SuggestedProductListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class SuggestedProductListDialogFragment extends BottomSheetDialogFragment {

    private RegisterProductViewModel mViewModel;
    private ProductListAdapter listAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_suggested_product_list, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // getViewModelStore().clear();
        Log.d( "SugdProductListDialogF", "Cleared  onDestroyView");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(requireActivity()).get(RegisterProductViewModel.class);
        listAdapter = new ProductListAdapter(new ProductListAdapter.ProductDiff(), product -> mViewModel.setProduct(product));

        final ProgressBar loadingBar = (ProgressBar) view.findViewById(R.id.loadingBar);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        final View suggestedResultsContainer = view.findViewById(R.id.suggestedResultsContainer);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        toolbar.setSubtitle(getResources().getQuantityString(R.plurals.matches_found, 0, 0) );
        suggestedResultsContainer.setVisibility(View.GONE);

        Runnable onNewProduct = () -> mViewModel.setEmptyProduct();

        MenuItem menuItemNewProduct = toolbar.getMenu().findItem(R.id.action_new);

        // this if it's in collapsed menu view
        menuItemNewProduct.setOnMenuItemClickListener(item -> {
            onNewProduct.run();
            return true;
        });

        // this as custom action view
        menuItemNewProduct.getActionView().setOnClickListener( v -> onNewProduct.run() );

        loadingBar.setVisibility( View.VISIBLE );
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter( listAdapter );

        mViewModel.getProducts().observe( getViewLifecycleOwner(), products -> {
            int size = products.size();
            Log.e( "TEST2", "submitting new list of size: " + size );
            toolbar.setSubtitle(getResources()
                    .getQuantityString(R.plurals.matches_found, size, size));

            if( size < 1 ){
                suggestedResultsContainer.setVisibility(View.GONE);
            }
            else {
                suggestedResultsContainer.setVisibility(View.VISIBLE);
            }
            loadingBar.setVisibility( View.VISIBLE );
            listAdapter.submitList( products );
            loadingBar.setVisibility( View.GONE );
        });
    }

}