package com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.holder;

import android.view.View;

import androidx.annotation.NonNull;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.google.android.material.chip.Chip;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.model.Cell;

public class ProductInstanceGroupCellQuantityViewHolder extends AbstractViewHolder {

    public Chip chip;

    public ProductInstanceGroupCellQuantityViewHolder(@NonNull View itemView) {
        super(itemView);
        chip = itemView.findViewById(R.id.cell_data);
    }

    public void bind(Cell model){
        chip.setText( String.valueOf(model.getData()) );
    }
}
