package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.SharedPantryViewModel;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups.model.ProductInstanceGroupInteractionsListener;
import com.jjak0b.android.trackingmypantry.ui.register_product.SharedProductViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.QuantityPickerBuilder;
import com.jjak0b.android.trackingmypantry.ui.util.SelectItemDialogBuilder;
import com.jjak0b.android.trackingmypantry.util.Callback;

import java.util.List;

public class ProductsGroupsBrowserBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private final static String TAG = "ProductsGroupsBrowserBottomSheetDialogFragment";
    private ProductsGroupsBrowserViewModel mViewModel;
    private SharedPantryViewModel mSharedPantryViewModel;
    private SharedProductViewModel mSharedProductViewModel;
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
        mSharedPantryViewModel = new ViewModelProvider(requireParentFragment()).get(SharedPantryViewModel.class);
        mSharedProductViewModel = new ViewModelProvider(requireParentFragment()).get(SharedProductViewModel.class);
        mViewModel = new ViewModelProvider(this).get(ProductsGroupsBrowserViewModel.class);

        mViewModel.setGroupsOf(
                mSharedProductViewModel.getItem(),
                mSharedPantryViewModel.getItem()
        );

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
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        TextView groupsInfo = view.findViewById(R.id.noGroupsInfo);
        groupsInfo.setVisibility(View.GONE);

        mSharedPantryViewModel.getItem().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    Pantry pantry = resource.getData();
                    toolbar.setLogo(null);
                    toolbar.setSubtitle(pantry.getName());
                    break;
                default:
                    toolbar.setLogo(R.drawable.loading_spinner);
                    toolbar.setSubtitle(null);
                    break;
            }
        });

        mViewModel.getItem().observe(getViewLifecycleOwner(), resource -> {
            Log.d(TAG, "Submitted new Pantry content items "  +resource );
            switch (resource.getStatus()) {
                case LOADING:
                    groupsInfo.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    List<ProductInstanceGroup> list = resource.getData();
                    if( list == null || list.isEmpty() ){
                        groupsInfo.setVisibility(View.VISIBLE);
                    }
                    else {
                        groupsInfo.setVisibility(View.GONE);
                    }
                    listAdapter.submitList(list);
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mViewModel.setItemSource(null);
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

        @Override
        public void onRemove(int groupPosition, ProductInstanceGroup group, int quantity) {
            mViewModel.delete(group, quantity);

            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            Futures.addCallback(mViewModel.delete(group, quantity),
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
                            Log.e(TAG, "Unable to delete " + group.toString() + ", cause: ", t);
                            recyclerView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(),
                                    getString(R.string.error_generic_failed_unknown, getString(R.string.option_remove_entry)),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    },
                    ContextCompat.getMainExecutor(requireContext())
            );
        }

        @Override
        public void onMove(int groupPosition, ProductInstanceGroup group) {

        }

        @Override
        public void onMore(int groupPosition, ProductInstanceGroup group, PopupMenu popup) {
            popup.getMenu().clear();
            popup.getMenuInflater()
                    .inflate( R.menu.popup_menu_product_instance_group_operations, popup.getMenu() );

            popup.setOnMenuItemClickListener( item -> {
                switch (item.getItemId()) {
                    case R.id.option_move_to:
                        new SelectItemDialogBuilder<Pantry>(requireContext())
                                .loadOn( mViewModel.getAvailablePantries(), getViewLifecycleOwner(), pantry -> {
                                    showQuantityPicker(1, group.getQuantity(), quantity -> {
                                        mViewModel.moveToPantry(
                                                group, pantry, quantity
                                        );
                                    });
                                })
                                .setCancelable(true)
                                .setNegativeButton(android.R.string.cancel, null)
                                .create()
                                .show();
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();

        }
    };

    public void showQuantityPicker(int min, int max, @NonNull Callback<Integer> onOk ) {
        new QuantityPickerBuilder(requireContext())
                .setMin(min)
                .setMax(max)
                .setPositiveButton(android.R.string.ok, onOk)
                .setNegativeButton(android.R.string.cancel , null )
                .setCancelable(true)
                .setTitle(R.string.product_quantity)
                .show();
    }
}