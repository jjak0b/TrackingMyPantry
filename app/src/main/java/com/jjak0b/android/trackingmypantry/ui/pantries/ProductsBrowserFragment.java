package com.jjak0b.android.trackingmypantry.ui.pantries;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjak0b.android.trackingmypantry.R;

public class ProductsBrowserFragment extends Fragment {

    private ProductsBrowserViewModel viewModel;
    private ProductListAdapter listAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel =
                new ViewModelProvider(this).get(ProductsBrowserViewModel.class);
        View root = inflater.inflate(R.layout.fragment_products_browser, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listAdapter = new ProductListAdapter( new ProductListAdapter.ProductDiff(), viewModel);

        final ProgressBar loadingBar = (ProgressBar) view.findViewById(R.id.productsloadingBar);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        final TextView listInfo = (TextView) view.findViewById( R.id.listInfo );

        loadingBar.setVisibility( View.VISIBLE );
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter( listAdapter );

        viewModel.getProducts().observe( getViewLifecycleOwner(), products -> {
            Log.e( "MyPantries", "submitting new list" );

            if( products.isEmpty() ){
                listInfo.setVisibility( View.VISIBLE );
            }
            else {
                listInfo.setVisibility( View.GONE );

            }
            loadingBar.setVisibility( View.VISIBLE );
            listAdapter.submitList( products );
            loadingBar.setVisibility( View.GONE );
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);

        fab.setOnClickListener( v -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_nav_pantries_to_registerProductBottomSheetDialogFragment);
            // d.show(getActivity().getSupportFragmentManager(), "dialog");
/*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
        });
    }

}