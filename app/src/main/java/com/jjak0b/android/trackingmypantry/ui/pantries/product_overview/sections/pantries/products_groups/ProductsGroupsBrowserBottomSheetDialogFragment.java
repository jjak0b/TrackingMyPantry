package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups;

import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelStore;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups.model.ProductInstanceGroupInteractionsListener;

import java.util.Iterator;
import java.util.LinkedList;
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
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
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
    };

    private void openPopupMenuForEntry(View parentAnchor, View anchor, int row, final ProductInstanceGroupListAdapter adapter){
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater()
                .inflate( R.menu.popup_menu_product_instance_group_operations, popup.getMenu() );
        SubMenu pantryListSubMenu = popup.getMenu()
                .findItem(R.id.option_move_to)
                .getSubMenu();
        int pantriesViewGroupID = ViewCompat.generateViewId();
        LiveData<List<Pantry>> livePantries = null;// mViewModel.getPantries();

        // observe pantry list to fill submenu entries as destinations
        Observer<List<Pantry>> observer = new Observer<List<Pantry>>() {
            @Override
            public void onChanged(List<Pantry> pantries) {
                boolean isNotEmpty = pantries != null && !pantries.isEmpty();
                popup.getMenu()
                        .findItem( R.id.option_move_to )
                        .setEnabled(isNotEmpty);
                if(isNotEmpty){
                    pantryListSubMenu.removeGroup(pantriesViewGroupID);
                    Iterator<Pantry> it = pantries.iterator();
                    int i = 0;
                    int itemID;
                    Pantry p;
                    while( it.hasNext() ){
                        p = it.next();
                        itemID = ViewCompat.generateViewId();
                        pantryListSubMenu.add( pantriesViewGroupID, itemID, i, p.toString() );
                        i++;
                    }
                }
            }
        };

        popup.setOnMenuItemClickListener( item -> {
            switch (item.getItemId()) {
                case R.id.option_delete:

                default:
                    if( pantriesViewGroupID == item.getGroupId() ){

                        int index = item.getOrder();
                        // get pantry data and observe it until first update to perform operation
                        livePantries.observe(getViewLifecycleOwner(), new Observer<List<Pantry>>() {
                            @Override
                            public void onChanged(List<Pantry> pantries) {
                                Pantry pantry = pantries != null ? pantries.get(index) : null;
                                ProductInstanceGroup entry = adapter.getCurrentList().get(row);
                                if( pantry != null && entry != null ) {

                                    NumberPicker quantityPicker = new NumberPicker(requireContext());
                                    quantityPicker.setMinValue(1);
                                    quantityPicker.setMaxValue(entry.getQuantity());
                                    new MaterialAlertDialogBuilder(requireContext())
                                            .setView(quantityPicker)
                                            .setCancelable(true)
                                            .setTitle(R.string.product_quantity)
                                            .setNegativeButton(android.R.string.cancel , null )
                                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                                mViewModel.moveProductInstanceGroupToPantry(
                                                        entry, pantry, quantityPicker.getValue()
                                                );
                                            })
                                            .setOnDismissListener(dialog -> {
                                                livePantries.removeObserver( this::onChanged );
                                            })
                                            .create()
                                            .show();
                                }
                            }
                        });
                        return true;
                    }
                    return false;
            }
        });

        popup.setOnDismissListener( menu -> {
            livePantries.removeObserver(observer);
        });

        livePantries.observe( getViewLifecycleOwner(), observer);

        popup.show();
    }



}