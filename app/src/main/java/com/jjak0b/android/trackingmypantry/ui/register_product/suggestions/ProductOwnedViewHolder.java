package com.jjak0b.android.trackingmypantry.ui.register_product.suggestions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;

public class ProductOwnedViewHolder<T extends Product> extends ProductViewHolder<T> {

    public ProductOwnedViewHolder(@NonNull View itemView ) {
        super(itemView);
    }

    public void bind(@NonNull T product, OnProductClickListener<T> onProductClickListener ){
        super.bind(product, onProductClickListener);
        favoriteContainer.setVisibility(View.VISIBLE);
    }

    static <T extends Product> ProductOwnedViewHolder<T> create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_suggested_product_list_dialog_list_dialog_item, parent, false);
        return new ProductOwnedViewHolder<>(view);
    }
}