package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

public class ProductViewHolder extends RecyclerView.ViewHolder  {

    private TextView title;
    private TextView description;
    private ImageView image;
    private Chip badge;
    private ChipGroup tags;
    private ExpandableLayout expandableLayout;
    private ImageButton actionExpandBtn;
    private FrameLayout fragmentContainer;
    public ProductViewHolder(@NonNull View itemView ) {
        super(itemView);
        title = itemView.findViewById(R.id.cardTitle);
        description = itemView.findViewById(R.id.cardDescription);
        image = itemView.findViewById(R.id.cardThumbnail);
        badge = itemView.findViewById(R.id.cardBadge);
        tags = itemView.findViewById(R.id.cardTags);
        expandableLayout = itemView.findViewById(R.id.expandable_layout);
        actionExpandBtn = itemView.findViewById(R.id.actionExpandBtn);
        fragmentContainer = itemView.findViewById(R.id.fragment_container);
    }


    public void bind(ProductWithTags productWithTags, ProductsBrowserViewModel viewModel, FragmentManager fm ){
        Product product = productWithTags.product;
        List<ProductTag> tagList = productWithTags.tags;

        fm.beginTransaction()
                .replace(fragmentContainer.getId(), PantriesBrowserFragment.newInstance( product ) )
                .commit();

        title.setText( product.getName());
        description.setText(product.getDescription());
        if( product.getImg() != null ){
            Glide
                .with(itemView)
                .load(product.getImg() )
                .fitCenter()
                .placeholder(R.drawable.loading_spinner)
                .into(image);
        }

        if( !tagList.isEmpty() ){
            for (ProductTag t : tagList ) {
                Chip chip = new Chip( itemView.getContext() );
                chip.setText( t.toString() );
                chip.setId(ViewCompat.generateViewId());
                tags.addView( chip );
            }
        }

        actionExpandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout.toggle();
                if( expandableLayout.isExpanded() ){
                    actionExpandBtn.setImageResource(R.drawable.ic_baseline_expand_less);
                }
                else {
                    actionExpandBtn.setImageResource(R.drawable.ic_baseline_expand_more);
                }
            }
        });
    }

    static ProductViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_products_browser_list_item, parent, false);
        return new ProductViewHolder(view);
    }
}