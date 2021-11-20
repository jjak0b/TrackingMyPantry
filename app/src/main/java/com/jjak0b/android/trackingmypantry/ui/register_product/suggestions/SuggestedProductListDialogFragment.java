package com.jjak0b.android.trackingmypantry.ui.register_product.suggestions;

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
import android.widget.Toast;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.AuthException;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.ui.register_product._RegisterProductViewModel;

import java.io.IOException;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     SuggestedProductListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class SuggestedProductListDialogFragment extends BottomSheetDialogFragment {

    private SuggestedProductsViewModel mViewModel;
    private _RegisterProductViewModel mSharedViewModel;
    private ProductListAdapter listAdapter;
    private String mParamBarcode;

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

        mParamBarcode = SuggestedProductListDialogFragmentArgs
                .fromBundle(getArguments()).getBarcode();

        mViewModel = new ViewModelProvider(requireParentFragment()).get(SuggestedProductsViewModel.class);
        mSharedViewModel = new ViewModelProvider(requireParentFragment()).get(_RegisterProductViewModel.class);

        listAdapter = new ProductListAdapter(new ProductListAdapter.ProductDiff(), product -> mViewModel.vote(product));

        final ProgressBar loadingBar = (ProgressBar) view.findViewById(R.id.loadingBar);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        final View suggestedResultsContainer = view.findViewById(R.id.suggestedResultsContainer);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        MenuItem menuItemNewProduct = toolbar.getMenu().findItem(R.id.action_new);

        // this if it's in collapsed menu view
        menuItemNewProduct.setOnMenuItemClickListener(item -> {
            onNewProduct();
            return true;
        });

        // this as custom action view
        menuItemNewProduct.getActionView().setOnClickListener( v -> onNewProduct() );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter( listAdapter );

        mViewModel.getProducts(mParamBarcode).observe( getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Status.LOADING) {
                loadingBar.setVisibility(View.VISIBLE);
                suggestedResultsContainer.setVisibility(View.GONE);
                toolbar.setSubtitle(getString(R.string.loading));
            }
            else {
                if( resource.getStatus() == Status.ERROR) {
                    if( resource.getError() instanceof AuthException) {
                        Toast.makeText(requireContext(),
                                getString(R.string.error_generic_failed_auth, getString(R.string.to_get_data)),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    else if( resource.getError() instanceof IOException) {
                        Toast.makeText(requireContext(),
                                getString(R.string.error_generic_failed_network, getString(R.string.to_get_data)),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    else { // if( resource.getError() instanceof RemoteException)
                        Toast.makeText(requireContext(),
                                getString(R.string.error_generic_failed_unknown, getString(R.string.to_get_data)),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }

                loadingBar.setVisibility(View.GONE);
                int size = resource.getData().size();
                toolbar.setSubtitle(getResources()
                        .getQuantityString(R.plurals.matches_found, size, size));

                if (size < 1)
                    suggestedResultsContainer.setVisibility(View.GONE);
                else
                    suggestedResultsContainer.setVisibility(View.VISIBLE);

                listAdapter.submitList(resource.getData());
            }
        });
    }

    void onNewProduct() {
        mViewModel.newProduct();
    }
}