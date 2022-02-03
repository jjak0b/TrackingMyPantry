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
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.ui.util.LoadUtil;

public class ProductViewHolder<T extends Product> extends RecyclerView.ViewHolder {

    @DrawableRes
    protected static final int RESOURCE_DEFAULT_PRODUCT_IMG = R.drawable.ic_baseline_product_placeholder;
    private Drawable LOADING_PLACEHOLDER;

    public TextView title;
    public TextView description;
    public ImageView image;
    public ViewGroup favoriteContainer;

    public ProductViewHolder(@NonNull View itemView ) {
        super(itemView);
        LOADING_PLACEHOLDER = LoadUtil.getProgressLoader(itemView.getContext());
        title = (TextView) itemView.findViewById(R.id.cardTitle);
        description = (TextView) itemView.findViewById(R.id.cardDescription);
        image = (ImageView) itemView.findViewById(R.id.cardThumbnail);
        favoriteContainer = itemView.findViewById(R.id.cardSubtitleContainer);
    }

    public void bind(@NonNull T product, OnProductClickListener<T> onProductClickListener ){
        title.setText(product.getName());
        description.setText(product.getDescription());
        favoriteContainer.setVisibility(View.GONE);

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

    static <T extends Product> ProductViewHolder<T> create(ViewGroup parent ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_suggested_product_list_dialog_list_dialog_item, parent, false);
        return new ProductViewHolder<>(view);
    }

    public interface OnProductClickListener<T> {

        void onClick(T product);
    }
}