package com.jjak0b.android.trackingmypantry.ui.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
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
        listAdapter = new ProductListAdapter( new ProductListAdapter.ProductDiff(), mViewModel );

        final ProgressBar loadingBar = (ProgressBar) view.findViewById(R.id.loadingBar);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

        loadingBar.setVisibility( View.VISIBLE );
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter( listAdapter );

        mViewModel.getProducts().observe( getViewLifecycleOwner(), products -> {
            Log.e( "TEST2", "submitting new list" );
            loadingBar.setVisibility( View.VISIBLE );
            listAdapter.submitList( products );
            loadingBar.setVisibility( View.GONE );
        });
    }

}