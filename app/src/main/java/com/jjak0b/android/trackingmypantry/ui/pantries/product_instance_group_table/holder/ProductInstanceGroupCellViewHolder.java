package com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.model.Cell;

/**
 * This is sample CellViewHolder class
 * This viewHolder must be extended from AbstractViewHolder class instead of RecyclerView.ViewHolder.
 */
public class ProductInstanceGroupCellViewHolder extends AbstractViewHolder {

    final LinearLayout cell_container;
    final TextView cellTextView;

    public ProductInstanceGroupCellViewHolder(View itemView) {
        super(itemView);
        cell_container = itemView.findViewById(R.id.cell_container);
        cellTextView = itemView.findViewById(R.id.cell_data);

    }
    public void bind( Cell model ){
        cellTextView.setText( String.valueOf( model.getData() ) );
    }
}