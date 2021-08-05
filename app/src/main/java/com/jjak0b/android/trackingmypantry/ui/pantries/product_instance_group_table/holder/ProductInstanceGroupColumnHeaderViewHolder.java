package com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.model.Cell;

/**
 * This is sample ColumnHeaderViewHolder class.
 * This viewHolder must be extended from AbstractViewHolder class instead of RecyclerView.ViewHolder.
 */
public class ProductInstanceGroupColumnHeaderViewHolder extends AbstractViewHolder {

    final ViewGroup column_header_container;
    final TextView column_header_textview;

    public ProductInstanceGroupColumnHeaderViewHolder(View itemView) {
        super(itemView);
        column_header_container = itemView.findViewById(R.id.column_header_container);
        column_header_textview = itemView.findViewById(R.id.column_header_textView);
    }

    public ViewGroup getContainer() {
        return column_header_container;
    }

    public void bind( Cell model ){
        column_header_textview.setText((Integer) model.getData());
    }
}
