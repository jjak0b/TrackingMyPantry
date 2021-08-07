package com.jjak0b.android.trackingmypantry.ui.pantries;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.listener.SimpleTableViewListener;
import com.google.android.material.snackbar.Snackbar;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.ProductInstanceGroupTableViewAdapter;

import java.util.Iterator;
import java.util.List;

public class ProductInstanceGroupBrowserFragment extends Fragment {

    private ProductInstanceGroupBrowserViewModel mViewModel;
    private ProductInstanceGroupTableViewAdapter tableAdapter;

    public static ProductInstanceGroupBrowserFragment newInstance(String productID, long PantryID ) {

        ProductInstanceGroupBrowserFragment fragment = new ProductInstanceGroupBrowserFragment();
        Bundle args = new Bundle();
        args.putString("productID", productID );
        args.putLong("pantryID", PantryID );
        fragment.setArguments( args );
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_instance_group_browser, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProductInstanceGroupBrowserViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        mViewModel.setDataParameters( args.getString("productID"), args.getLong("pantryID") );

        ViewGroup tableContainer = view.findViewById(R.id.tableContainer);
        TableView tableView = view.findViewById(R.id.tableView);

        // hide corner and column of row number
        tableView.setShowCornerView( false );
        // block focus when items are changed
        // prevent: java.lang.IllegalArgumentException: parameter must be a descendant of this view
        tableView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        tableAdapter = new ProductInstanceGroupTableViewAdapter();
        tableView.setAdapter(tableAdapter);
        tableView.setTableViewListener(new SimpleTableViewListener() {
            @Override
            public void onCellClicked(@NonNull RecyclerView.ViewHolder cellView, int column, int row) {
                // tableView.setSelectedRow(row);
            }

            @Override
            public void onCellDoubleClicked(@NonNull RecyclerView.ViewHolder cellView, int column, int row) {
                // tableView.setSelectedRow(row);

            }

            @Override
            public void onCellLongPressed(@NonNull RecyclerView.ViewHolder cellView, int column, int row) {
                // tableView.setSelectedRow(row);
                openPopupMenuForEntry(cellView.itemView, row );
            }
        });

        mViewModel.getItems().observe( getViewLifecycleOwner(), itemsList -> {
            // must not be empty: https://github.com/evrencoskun/TableView/issues/26
            if( !itemsList.isEmpty() ){
                tableContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        tableContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        Log.d( ProductInstanceGroupBrowserFragment.class.getName(), "update Table width on GlobalLayout");
                        tableAdapter.setRowWidth( tableContainer.getWidth() );
                        tableAdapter.submitList( itemsList );
                    }
                });
            }
        });
    }

    private void openPopupMenuForEntry(View anchor, int row ){
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater()
                .inflate( R.menu.popup_menu_product_instance_group_operations, popup.getMenu() );
        SubMenu pantryListSubMenu = popup.getMenu()
                .findItem(R.id.option_move_to)
                .getSubMenu();
        int pantriesViewGroupID = ViewCompat.generateViewId();

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
                    deleteEntry(row);
                default:
                    if( pantriesViewGroupID == item.getGroupId() ){
                        int index = item.getOrder();

                        LiveData<Pantry> fetchPantry = Transformations.map(
                                mViewModel.getPantries(),
                                pantries -> pantries != null ? pantries.get(index) : null
                        );
                        fetchPantry.observe(getViewLifecycleOwner(), new Observer<Pantry>() {
                            @Override
                            public void onChanged(Pantry pantry) {
                                ProductInstanceGroup entry = tableAdapter.getRowItem(row);
                                if( pantry != null && entry != null ) {
                                    mViewModel.moveProductInstanceGroupToPantry(entry, pantry);
                                    fetchPantry.removeObserver( this::onChanged );
                                }
                            }
                        });
                        return true;
                    }
                    return false;
            }
        });

        popup.setOnDismissListener( menu -> {
            mViewModel.getPantries().observe(getViewLifecycleOwner(), observer );
        });

        mViewModel.getPantries().observe( getViewLifecycleOwner(), observer);

        popup.show();
    }

    private void deleteEntry(int position) {
        ProductInstanceGroup entry = tableAdapter.getRowItem( position );
        tableAdapter.removeRow(position);

        final Snackbar snackbar = Snackbar.make(requireView(), " is Deleted", Snackbar.LENGTH_LONG);

        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableAdapter.addRowItem( position, entry );
            }
        });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event == DISMISS_EVENT_TIMEOUT) {
                    mViewModel.deleteProductInstanceGroup( entry );
                }
            }
        });
        snackbar.show();
    }
}