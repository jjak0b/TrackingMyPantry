package com.jjak0b.android.trackingmypantry.ui.register_product.suggestions;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.ui.util.LoadUtil;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    @DrawableRes
    protected static final int RESOURCE_DEFAULT_PRODUCT_IMG = R.drawable.ic_baseline_product_placeholder;
    private Drawable LOADING_PLACEHOLDER;

    private TextView title;
    private TextView description;
    private ImageView image;

    public ProductViewHolder(@NonNull View itemView ) {
        super(itemView);
        LOADING_PLACEHOLDER = LoadUtil.getProgressLoader(itemView.getContext());
        title = (TextView) itemView.findViewById(R.id.cardTitle);
        description = (TextView) itemView.findViewById(R.id.cardDescription);
        image = (ImageView) itemView.findViewById(R.id.cardThumbnail);
    }

    public void bind(@NonNull UserProduct product, OnProductClickListener onProductClickListener ){
        title.setText( product.getName());
        description.setText(product.getDescription());

        this.itemView.setOnClickListener(v -> {
            if( onProductClickListener != null )
                onProductClickListener.onClick(product);
        });

        Glide
            .with(itemView)
            .load(product.getImg())
            .fitCenter()
            .placeholder(LOADING_PLACEHOLDER)
            .fallback(RESOURCE_DEFAULT_PRODUCT_IMG)
            .into(image);
    }

    static ProductViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_suggested_product_list_dialog_list_dialog_item, parent, false);
        return new ProductViewHolder(view);
    }

    public interface OnProductClickListener {

        void onClick(UserProduct product);
    }
}