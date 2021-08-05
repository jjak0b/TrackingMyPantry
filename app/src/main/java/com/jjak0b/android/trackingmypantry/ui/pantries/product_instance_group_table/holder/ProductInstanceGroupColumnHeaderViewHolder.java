package com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.model.Cell;

/**
 * This is sample ColumnHeaderViewHolder class.
 * This viewHolder must be extended from AbstractViewHolder class instead of RecyclerView.ViewHolder.
 */
public class ProductInstanceGroupColumnHeaderViewHolder extends AbstractViewHolder {

    final LinearLayout column_header_container;
    final TextView column_header_textview;

    public ProductInstanceGroupColumnHeaderViewHolder(View itemView) {
        super(itemView);
        column_header_container = itemView.findViewById(R.id.column_header_container);
        column_header_textview = itemView.findViewById(R.id.column_header_textView);
    }

    public void bind( Cell model ){

        column_header_textview.setText((Integer) model.getData());

        // If your TableView should have auto resize for cells & columns.
        // Then you should consider the below lines. Otherwise, you can ignore them.
        // It is necessary to remeasure itself.
       /* column_header_container.getLayoutParams().width = LinearLayout
                .LayoutParams.WRAP_CONTENT;
        column_header_textview.requestLayout();*/
    }
}
