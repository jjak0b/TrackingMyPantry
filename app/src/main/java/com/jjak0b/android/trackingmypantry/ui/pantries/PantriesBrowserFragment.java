package com.jjak0b.android.trackingmypantry.ui.pantries;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Product;

public class PantriesBrowserFragment extends Fragment {

    private PantriesBrowserViewModel mViewModel;
    private PantryListAdapter listAdapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantries_browser, container, false);
    }

    public static PantriesBrowserFragment newInstance(Product product ) {

        Bundle args = new Bundle();
        args.putString("productID", product.getId() );

        PantriesBrowserFragment fragment = new PantriesBrowserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String productID = getArguments().getString("productID");

        mViewModel = new ViewModelProvider(this).get(PantriesBrowserViewModel.class);
        final ProgressBar loadingBar = (ProgressBar) view.findViewById(R.id.pantriesLoadingBar);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        final TextView listInfo = (TextView) view.findViewById( R.id.listInfo );

        listAdapter = new PantryListAdapter( new PantryListAdapter.ProductDiff(), mViewModel);
        loadingBar.setVisibility( View.VISIBLE );
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter( listAdapter );


        if( productID != null ){
            mViewModel.setProductID( productID );
        }

        mViewModel.getPantriesList().observe( getViewLifecycleOwner(), pantryWithProductInstanceGroupsList -> {
            Log.e( "MyPantries", "submitting new list" );
            if( pantryWithProductInstanceGroupsList.isEmpty() ){
                listInfo.setVisibility( View.VISIBLE );
            }
            else {
                listInfo.setVisibility( View.GONE );

            }
            loadingBar.setVisibility( View.VISIBLE );
            listAdapter.submitList( pantryWithProductInstanceGroupsList );
            loadingBar.setVisibility( View.GONE );
        });
    }
}