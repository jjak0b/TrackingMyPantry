package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelStore;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups.model.ProductInstanceGroupInteractionsListener;

import java.util.List;

public class ProductsGroupsBrowserBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private final static String TAG = "ProductsGroupsBrowserBottomSheetDialogFragment";
    private ProductsGroupsBrowserViewModel mViewModel;
    private ProductInstanceGroupListAdapter listAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    public static ProductsGroupsBrowserBottomSheetDialogFragment newInstance() {
        return new ProductsGroupsBrowserBottomSheetDialogFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products_groups_browser, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireParentFragment()).get(ProductsGroupsBrowserViewModel.class);
        listAdapter = new ProductInstanceGroupListAdapter(new ProductInstanceGroupListAdapter.ProductDiff(), interactionsListener) {
            @NonNull
            @Override
            public ViewModelStore getViewModelStore() {
                return requireParentFragment().getViewModelStore();
            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.list);
        progressBar = view.findViewById(R.id.pantryLoadingBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(listAdapter);

        mViewModel.getGroups().observe(getViewLifecycleOwner(), productInstanceGroups -> {
            listAdapter.submitList(productInstanceGroups);
        });
    }

    final ProductInstanceGroupInteractionsListener interactionsListener = new ProductInstanceGroupInteractionsListener() {
        @Override
        public void onItemClicked(int pantryPosition, View pantryView, Pantry item, List<ProductInstanceGroup> content) {

        }

        @Override
        public void onItemLongClicked(int pantryPosition, View pantryView, Pantry item, List<ProductInstanceGroup> content) {

        }

        @Override
        public void onConsume(int groupPosition, ProductInstanceGroup group, int amount) {

            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            Futures.addCallback(mViewModel.consume(group, amount),
                    new FutureCallback<Void>() {
                        @Override
                        public void onSuccess(@Nullable Void result) {
                            recyclerView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            // Toast.makeText(requireContext(),
                            //         "Yum",
                            //         Toast.LENGTH_SHORT
                            // ).show();*/
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.e(TAG, "Unable to consume " + group.toString() + ", cause: ", t);
                            recyclerView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(),
                                    getString(R.string.error_generic_failed_unknown, getString(R.string.option_consume)),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    },
                    ContextCompat.getMainExecutor(requireContext())
            );
        }
    };
}