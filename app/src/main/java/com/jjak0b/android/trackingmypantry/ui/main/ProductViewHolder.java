package com.jjak0b.android.trackingmypantry.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Product;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    private TextView title;
    private TextView description;

    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.cardTitle);
        description = itemView.findViewById(R.id.cardDescription);
    }

    public void bind(Product product){
        title.setText( product.getName());
        description.setText(product.getDescription());
    }

    static ProductViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_suggested_product_list_dialog_list_dialog_item, parent, false);
        return new ProductViewHolder(view);
    }
}