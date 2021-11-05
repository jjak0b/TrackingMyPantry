package com.jjak0b.android.trackingmypantry.ui.products;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjak0b.android.trackingmypantry.R;

public class ProductsBrowserFragment extends Fragment {

    private ProductsBrowserViewModel viewModel;
    private ProductsSearchFilterViewModel searchViewModel;
    private ProductListAdapter listAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel =
                new ViewModelProvider(this).get(ProductsBrowserViewModel.class);
        searchViewModel =
                new ViewModelProvider(requireParentFragment()).get(ProductsSearchFilterViewModel.class);
        View root = inflater.inflate(R.layout.fragment_products_browser, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ProgressBar loadingBar = (ProgressBar) view.findViewById(R.id.productsloadingBar);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        final TextView listInfo = (TextView) view.findViewById( R.id.listInfo );

        listAdapter = new ProductListAdapter(new ProductListAdapter.ProductDiff(), onProductClick );

        loadingBar.setVisibility( View.VISIBLE );
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter( listAdapter );

        viewModel.getProductsWithTags().observe( getViewLifecycleOwner(), productsWTags -> {
            Log.e( "MyProducts", "submitting new list from " + this.toString() );

            if( productsWTags.isEmpty() ){
                listInfo.setVisibility( View.VISIBLE );
            }
            else {
                listInfo.setVisibility( View.GONE );

            }
            loadingBar.setVisibility( View.VISIBLE );
            listAdapter.submitList( productsWTags );
            loadingBar.setVisibility( View.GONE );
            listAdapter.notifyDataSetChanged();
        });

        searchViewModel.onSearch().observe(getViewLifecycleOwner(), searchState -> {
            viewModel.setFilterState(searchState);
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);

        fab.setOnClickListener( onFabClick );
    }

    private View.OnClickListener onFabClick = v -> {
        Navigation.findNavController(getView())
                .navigate(ProductsBrowserFragmentDirections.openRegisterProduct());
    };

    private ProductListAdapter.OnProductClick onProductClick = product -> {
        Navigation.findNavController(getView())
                .navigate(ProductsBrowserFragmentDirections.openProduct(product.getId(), product.getName() ));
    };

    private final SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            searchViewModel.setSearchQuery(newText);
            searchViewModel.search();

            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            searchViewModel.setSearchQuery(query);
            searchViewModel.search();
            return true;
        }
    };

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.products_browser_menu, menu);
        // Associate searchable configuration with the SearchView


        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchViewModel.getSearchQuery().observe(getViewLifecycleOwner(), s -> {
            searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                searchView.setQuery(s, !hasFocus );
            });
        });

        searchViewModel.getSearchTags().observe(getViewLifecycleOwner(), productTags -> {
            searchViewModel.search();
        });

        searchView.setOnQueryTextListener(onQueryTextListener);
    }
}