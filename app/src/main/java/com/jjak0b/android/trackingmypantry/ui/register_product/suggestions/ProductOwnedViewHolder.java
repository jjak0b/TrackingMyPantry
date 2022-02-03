package com.jjak0b.android.trackingmypantry.ui.register_product.suggestions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;

public class ProductOwnedViewHolder<T extends Product> extends ProductViewHolder<T> {

    private TextView title;

    public ProductOwnedViewHolder(@NonNull View itemView ) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.cardTitle);
    }


    public void bind(@NonNull T product, OnProductClickListener<T> onProductClickListener ){
        super.bind(product, onProductClickListener);
        title.setText( "OWNED " + product.getName()); // TODO: fix
    }

    static <T extends Product> ProductOwnedViewHolder<T> create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_suggested_product_list_dialog_list_dialog_item, parent, false);
        return new ProductOwnedViewHolder<>(view);
    }
}