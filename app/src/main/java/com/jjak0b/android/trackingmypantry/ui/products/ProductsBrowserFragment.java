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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;
import com.jjak0b.android.trackingmypantry.ui.util.InputUtil;
import com.jjak0b.android.trackingmypantry.ui.util.TagsPicker.SharedTagsViewModel;

import java.util.List;

public class ProductsBrowserFragment extends Fragment {

    public static final String TAG = "ProductsBrowserFragment";
    private ProductsBrowserViewModel viewModel;
    private ProductsSearchFilterViewModel searchViewModel;
    private ProductListAdapter listAdapter;
    private SharedTagsViewModel mTagsPickerViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTagsPickerViewModel = new ViewModelProvider(requireActivity()).get(SharedTagsViewModel.class);

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

        viewModel.getProductsWithTags().observe( getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    loadingBar.setVisibility( View.VISIBLE );
                    break;
                case SUCCESS:
                    List<ProductWithTags> items = resource.getData();
                    if( items.isEmpty() ){
                        listInfo.setVisibility( View.VISIBLE );
                    }
                    else {
                        listInfo.setVisibility( View.GONE );
                    }
                    listAdapter.submitList( items );
                    loadingBar.setVisibility( View.GONE );
                    listAdapter.notifyDataSetChanged();
                    break;
                case ERROR:
                    String errorMsg = ErrorsUtils.getErrorMessage(requireContext(), resource.getError(), TAG );
                    if( errorMsg != null ) {
                        new AlertDialog.Builder(requireContext())
                                .setMessage(errorMsg)
                                .setTitle(android.R.string.dialog_alert_title)
                                .setPositiveButton(android.R.string.ok, null )
                                .setNeutralButton(R.string.action_retry, (dialog, which) -> {
                                    searchViewModel.search();
                                })
                                .show();
                    }
                    break;
            }
        });

        // From tags picker to search ViewModel
        mTagsPickerViewModel.getItem().observe(getViewLifecycleOwner(), resource -> {
            if( resource.getStatus() != Status.LOADING ) {
                Log.d(TAG, "Updating filter tags" + resource );
                searchViewModel.setSearchTags(resource.getData());
            }
        });

        // search trigger logic
        searchViewModel.getSearchTags().observe(getViewLifecycleOwner(), productTags -> {
            searchViewModel.search();
        });

        // on search trigger, then update the filter on main ViewMdel
        searchViewModel.onSearch().observe(getViewLifecycleOwner(), searchState -> {
            Log.d(TAG, "got request for a new search ... " + searchState );
            viewModel.setFilterState(searchState);
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);

        fab.setOnClickListener( onFabClick );
    }

    private View.OnClickListener onFabClick = v -> {
        Navigation.findNavController(getView())
                .navigate(ProductsBrowserFragmentDirections.openRegisterProduct(null));
    };

    private ProductListAdapter.OnProductClick onProductClick = product -> {
        Navigation.findNavController(getView())
                .navigate(ProductsBrowserFragmentDirections.openProduct(product.getBarcode(), product.getName() ));
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
                InputUtil.setQuery(searchView, s, !hasFocus);
            });
        });

        searchView.setOnQueryTextListener(onQueryTextListener);
    }
}