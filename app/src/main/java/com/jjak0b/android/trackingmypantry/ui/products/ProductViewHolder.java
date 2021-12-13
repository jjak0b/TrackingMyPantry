package com.jjak0b.android.trackingmypantry.ui.products;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;

import java.util.List;

public class ProductViewHolder extends RecyclerView.ViewHolder  {

    @DrawableRes
    protected static final int RESOURCE_LOADING_PRODUCT_IMG = R.drawable.loading_spinner;
    @DrawableRes
    protected static final int RESOURCE_DEFAULT_PRODUCT_IMG = R.drawable.ic_baseline_product_placeholder;
    private TextView title;
    private TextView description;
    private ImageView image;
    private ChipGroup tags;

    public ProductViewHolder(@NonNull View itemView ) {
        super(itemView);
        title = itemView.findViewById(R.id.cardTitle);
        description = itemView.findViewById(R.id.cardDescription);
        image = itemView.findViewById(R.id.cardThumbnail);
        tags = itemView.findViewById(R.id.cardTags);
    }


    public void bind(ProductWithTags productWithTags ){
        Product product = productWithTags.product;
        List<ProductTag> tagList = productWithTags.tags;
        title.setText( product.getName());
        description.setText(product.getDescription());
        Glide
            .with(itemView)
            .load(product.getImg())
            .fitCenter()
            .placeholder(RESOURCE_LOADING_PRODUCT_IMG)
            .fallback(RESOURCE_DEFAULT_PRODUCT_IMG)
            .into(image);

        if( !tagList.isEmpty() ){
            tags.removeAllViews();
            for (ProductTag t : tagList ) {
                Chip chip = new Chip( itemView.getContext() );
                chip.setText( t.toString() );
                chip.setId(ViewCompat.generateViewId());
                tags.addView( chip );
            }
        }
    }

    public static ProductViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_products_browser_list_item, parent, false);
        return new ProductViewHolder(view);
    }
}