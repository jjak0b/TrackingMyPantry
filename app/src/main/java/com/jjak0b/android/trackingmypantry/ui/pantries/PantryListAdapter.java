package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.ProductInstanceGroupTableViewAdapter;

public class PantryListAdapter extends ListAdapter<PantryWithProductInstanceGroups, PantryViewHolder> {

    private PantriesBrowserViewModel viewModel;
    private ProductInstanceGroupTableViewAdapter tableAdapter;
    private FragmentManager fm;

    protected PantryListAdapter(@NonNull DiffUtil.ItemCallback<PantryWithProductInstanceGroups> diffCallback, @NonNull PantriesBrowserViewModel viewModel, FragmentManager fm ) {
        super(diffCallback);
        this.viewModel = viewModel;
        this.fm = fm;
        this.tableAdapter = new ProductInstanceGroupTableViewAdapter();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PantryViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryWithProductInstanceGroups current = getItem(position);
        holder.bind(current, viewModel, fm );
        // must not be empty: https://github.com/evrencoskun/TableView/issues/26
        if( !current.instances.isEmpty() ){
            holder.tableView.setShowCornerView( false );
            holder.tableView.setRowHeaderWidth(0);
            holder.tableView.setAdapter(tableAdapter);
            tableAdapter.setItems( current.instances );
        }
    }

    static class ProductDiff extends DiffUtil.ItemCallback<PantryWithProductInstanceGroups> {
        @Override
        public boolean areItemsTheSame(@NonNull PantryWithProductInstanceGroups oldItem, @NonNull PantryWithProductInstanceGroups newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryWithProductInstanceGroups oldItem, @NonNull PantryWithProductInstanceGroups newItem) {
            return oldItem.pantry.getId() != newItem.pantry.getId();
        }
    }


}