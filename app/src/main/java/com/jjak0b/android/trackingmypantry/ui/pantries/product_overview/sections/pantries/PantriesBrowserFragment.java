package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries;

import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.ProductInstanceGroupTableViewAdapter;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.ProductOverviewViewModel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PantriesBrowserFragment extends Fragment {

    private PantriesBrowserViewModel mViewModel;
    private ProductOverviewViewModel mProductViewModel;
    private PantryListAdapter listAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PantriesBrowserViewModel.class);
        mProductViewModel = new ViewModelProvider(requireParentFragment()).get(ProductOverviewViewModel.class);
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

        mProductViewModel.getProduct().observe(getViewLifecycleOwner(), productWithTags -> {
            Log.e( "MyPantries", "setting productID" );
            if( productWithTags != null ){
                mViewModel.setProductID(productWithTags.product.getId());
            }
        });

        mViewModel.getList().observe( getViewLifecycleOwner(), pantriesWGroups -> {
            Log.e( "MyPantries", "submitting new list of " + pantriesWGroups.size() + "elements from " +  this.toString() + " " );
            if( pantriesWGroups.isEmpty() ){
                listInfo.setVisibility( View.VISIBLE );
            }
            else {
                listInfo.setVisibility( View.GONE );

            }
            loadingBar.setVisibility( View.VISIBLE );
            listAdapter.submitList( pantriesWGroups );
            loadingBar.setVisibility( View.GONE );
        });
    }

    final PantryInteractionsListener pantryInteractionsListener = new PantryInteractionsListener() {
        @Override
        public void onItemClicked(int pantryPosition, View pantryView, int pantryItemPosition, View pantryItemView, ProductInstanceGroupTableViewAdapter pantryItemsAdapter) {

        }

        @Override
        public void onItemLongClicked(int pantryPosition, View pantryView, int pantryItemPosition, View pantryItemView, ProductInstanceGroupTableViewAdapter pantryItemsAdapter) {
            openPopupMenuForEntry(pantryView, pantryItemView, pantryItemPosition, pantryItemsAdapter );
        }
    };

    private void openPopupMenuForEntry(View parentAnchor, View anchor, int row, final ProductInstanceGroupTableViewAdapter tableAdapter){
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater()
                .inflate( R.menu.popup_menu_product_instance_group_operations, popup.getMenu() );
        SubMenu pantryListSubMenu = popup.getMenu()
                .findItem(R.id.option_move_to)
                .getSubMenu();
        int pantriesViewGroupID = ViewCompat.generateViewId();
        LiveData<List<Pantry>> livePantries = mViewModel.getPantries();

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
                    deleteEntry(row, tableAdapter, parentAnchor);
                default:
                    if( pantriesViewGroupID == item.getGroupId() ){

                        int index = item.getOrder();
                        // get pantry data and observe it until first update to perform operation
                        livePantries.observe(getViewLifecycleOwner(), new Observer<List<Pantry>>() {
                            @Override
                            public void onChanged(List<Pantry> pantries) {
                                Pantry pantry = pantries != null ? pantries.get(index) : null;
                                ProductInstanceGroup entry = tableAdapter.getRowItem(row);
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

    // queue used to store pending entry to be deleted
    private LinkedList<ProductInstanceGroup> deletionQueue = new LinkedList<>();

    private void deleteEntry(int position, ProductInstanceGroupTableViewAdapter tableAdapter, View anchor) {

        final Snackbar snackbar = Snackbar.make(requireView(), R.string.product_entry_removed_from_pantry, Snackbar.LENGTH_LONG);
        // set snackbar anchor to entry view item
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)snackbar.getView().getLayoutParams();
        layoutParams.setAnchorId(anchor.getId());
        layoutParams.anchorGravity = Gravity.BOTTOM;
        snackbar.getView().setLayoutParams(layoutParams);

        ProductInstanceGroup entry = tableAdapter.getRowItem( position );
        snackbar.setAction(R.string.action_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableAdapter.addRowItem( position, deletionQueue.removeLast() );
            }
        });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                tableAdapter.removeRowItem(position);
                deletionQueue.addLast(entry);
                super.onShown(sb);
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event == DISMISS_EVENT_TIMEOUT ||
                    event == DISMISS_EVENT_SWIPE ||
                    event == DISMISS_EVENT_ACTION
                ) {
                    clearDeletionQueue();
                }
            }
        });
        snackbar.show();
    }

    void clearDeletionQueue() {
        Object[] from = deletionQueue.toArray();
        ProductInstanceGroup[] to = new ProductInstanceGroup[from.length];
        System.arraycopy(from, 0, to, 0, from.length);
        deletionQueue.clear();

        mViewModel.deleteProductInstanceGroup(to);
    }
}