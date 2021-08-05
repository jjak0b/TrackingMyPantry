package com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.holder;

import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.model.Cell;

public class ProductInstanceGroupCellStatusViewHolder extends AbstractViewHolder {

    public SeekBar statusBar;

    public ProductInstanceGroupCellStatusViewHolder(@NonNull View itemView) {
        super(itemView);
        statusBar = itemView.findViewById(R.id.cell_data);
    }

    public void bind(Cell model){
        statusBar.setProgress((Integer) model.getData());
    }
}
