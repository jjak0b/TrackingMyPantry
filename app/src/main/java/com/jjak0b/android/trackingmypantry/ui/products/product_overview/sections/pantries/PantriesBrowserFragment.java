package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries;

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
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.results.PantryDetails;
import com.jjak0b.android.trackingmypantry.ui.register_product.SharedProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ErrorsUtils;

import java.util.List;

public class PantriesBrowserFragment extends Fragment {

    private static final String TAG = "PantriesBrowserFragment";
    private PantriesBrowserViewModel mViewModel;
    private SharedProductViewModel mProductViewModel;
    private PantryListAdapter listAdapter;
    private SharedPantryViewModel mSharedPantryViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PantriesBrowserViewModel.class);
        mProductViewModel = new ViewModelProvider(requireParentFragment()).get(SharedProductViewModel.class);
        mSharedPantryViewModel = new ViewModelProvider(requireParentFragment()).get(SharedPantryViewModel.class);
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

        mProductViewModel.getItem().observe(getViewLifecycleOwner(), mViewModel::setProduct);

        mViewModel.getList().observe( getViewLifecycleOwner(), resource -> {
            Log.e(TAG, "newPantries:"+resource);
            switch (resource.getStatus()) {
                case LOADING:
                    loadingBar.setVisibility( View.VISIBLE );
                    break;
                case SUCCESS:
                    List<PantryDetails> pantriesWGroups = resource.getData();
                    if( pantriesWGroups == null || pantriesWGroups.isEmpty() ){
                        listInfo.setVisibility( View.VISIBLE );
                    }
                    else {
                        Log.e( TAG, "submitting new list of " + pantriesWGroups.size() + "elements from " +  this.toString() + " " );
                        listInfo.setVisibility( View.GONE );
                    }
                    listAdapter.submitList( pantriesWGroups );
                    loadingBar.setVisibility( View.GONE );
                    break;
                case ERROR:
                    listInfo.setVisibility( View.VISIBLE );
                    loadingBar.setVisibility( View.GONE );
                    ErrorsUtils.getErrorMessage(requireContext(), resource.getError(), TAG);
                    break;
            }
        });

        // update groups browser on current pantry update
        mSharedPantryViewModel.setItemSource(mViewModel.getCurrentPantry());
    }

    final PantryInteractionsListener pantryInteractionsListener = new PantryInteractionsListener() {
        @Override
        public void onItemClicked(int pantryPosition, View pantryView, PantryDetails item) {
            NavController navController = Navigation.findNavController(requireView());
            NavDirections direction = PantriesBrowserFragmentDirections.actionShowPantryContent();
            if( navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getAction(direction.getActionId()) != null ){
                mViewModel.setCurrentPantry(item.pantry);
                navController.navigate(direction);
            }
        }

        @Override
        public void onItemLongClicked(int pantryPosition, View pantryView, PantryDetails item) {

        }
    };

}