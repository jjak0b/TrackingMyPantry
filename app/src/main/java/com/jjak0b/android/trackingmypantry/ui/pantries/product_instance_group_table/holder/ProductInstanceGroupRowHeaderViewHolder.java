package com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.holder;

import android.view.View;
import android.widget.TextView;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.model.Cell;

/**
 * This is sample RowHeaderViewHolder class.
 * This viewHolder must be extended from AbstractViewHolder class instead of RecyclerView.ViewHolder.
 */
public class ProductInstanceGroupRowHeaderViewHolder extends AbstractViewHolder {

    final TextView row_header_textview;

    public ProductInstanceGroupRowHeaderViewHolder(View itemView) {
        super(itemView);
        row_header_textview = itemView.findViewById(R.id.row_header_textView);
    }

    public void bind( Cell model, int position ){
        row_header_textview.setText( String.valueOf(position) );
    }
}
