package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;

import net.cachapa.expandablelayout.ExpandableLayout;

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


    public void bind(Pantry pantry, String productID, FragmentManager fm){

        fm.beginTransaction()
                .replace(fragmentContainer.getId(), ProductInstanceGroupBrowserFragment.newInstance( productID, pantry.getId() ) )
                .commit();

        title.setText( pantry.getName() );
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