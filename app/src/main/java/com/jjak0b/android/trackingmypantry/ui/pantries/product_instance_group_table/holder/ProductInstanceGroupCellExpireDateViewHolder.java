package com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.model.Cell;

import java.text.DateFormat;
import java.util.Date;

public class ProductInstanceGroupCellExpireDateViewHolder extends AbstractViewHolder {

    public TextView textView;

    public ProductInstanceGroupCellExpireDateViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.cell_data);
    }

    public void bind(Cell model){
        Date date = (Date) model.getData();
        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(itemView.getContext());
        textView.setText( dateFormat.format(date) );
    }
}
