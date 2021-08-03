package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

public class PantryViewHolder extends RecyclerView.ViewHolder  {

    private TextView title;
    private TextView description;
    private ImageView image;
    private Chip badge;
    private ChipGroup tags;
    private ExpandableLayout expandableLayout;
    private ImageButton actionExpandBtn;
    private FrameLayout fragmentContainer;

    public PantryViewHolder(@NonNull View itemView ) {
        super(itemView);
        title = itemView.findViewById(R.id.cardTitle);
        badge = itemView.findViewById(R.id.cardBadge);
        expandableLayout = itemView.findViewById(R.id.expandable_layout);
        actionExpandBtn = itemView.findViewById(R.id.actionExpandBtn);
        fragmentContainer = itemView.findViewById(R.id.fragment_container);
    }


    public void bind(PantryWithProductInstanceGroups pantryWProducts, PantriesBrowserViewModel viewModel){
        Pantry pantry = pantryWProducts.pantry;
        List<ProductInstanceGroup> itemsList = pantryWProducts.instances;

/*
        FragmentManager mFragmentManager = FragmentManager.findFragment(itemView).getActivity()
                .getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(fragmentContainer.getId(), ProductInstanceGroupListFragment.newInstance( product, pantry ) )
                .commit();
*/

        title.setText( pantry.getName() );
        badge.setText( String.valueOf( itemsList.size() )  );

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

    static PantryViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_pantries_browser_list_item, parent, false);
        return new PantryViewHolder(view);
    }
}