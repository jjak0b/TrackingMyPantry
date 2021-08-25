package com.jjak0b.android.trackingmypantry.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Product;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    private TextView title;
    private TextView description;
    private ImageView image;

    public ProductViewHolder(@NonNull View itemView ) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.cardTitle);
        description = (TextView) itemView.findViewById(R.id.cardDescription);
        image = (ImageView) itemView.findViewById(R.id.cardThumbnail);
    }

    public void bind(@NonNull Product product, OnProductClickListener onProductClickListener ){
        title.setText( product.getName());
        description.setText(product.getDescription());

        this.itemView.setOnClickListener(v -> {
            if( onProductClickListener != null )
                onProductClickListener.onClick(product);
        });

        if( product.getImg() != null ){
            Glide
                .with(itemView)
                .load(product.getImg() )
                .fitCenter()
                .placeholder(R.drawable.loading_spinner)
                .into(image);
        }
    }

    static ProductViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_suggested_product_list_dialog_list_dialog_item, parent, false);
        return new ProductViewHolder(view);
    }

    public interface OnProductClickListener {

        void onClick(Product product);
    }
}