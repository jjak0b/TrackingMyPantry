package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
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
    private ViewGroup fragmentContainer;
    private FrameLayout fragmentLayout;
    public PantryViewHolder(@NonNull View itemView ) {
        super(itemView);
        title = itemView.findViewById(R.id.cardTitle);
        badge = itemView.findViewById(R.id.cardBadge);
        expandableLayout = itemView.findViewById(R.id.expandable_layout);
        actionExpandBtn = itemView.findViewById(R.id.actionExpandBtn);
        fragmentContainer = itemView.findViewById(R.id.fragment_container);


        // isExpanded = expandableLayout.isExpanded();
        Log.e("a", "new instance");
    }


    public void bind(Pantry pantry, String productID, FragmentManager fm){
        Log.e("a", "bind " + pantry);

        if(fragmentContainer.getChildCount() < 1 ){
            fragmentLayout = new FrameLayout(itemView.getContext());
            fragmentLayout.setId(ViewCompat.generateViewId());
            fragmentLayout.setLayoutParams( fragmentContainer.getLayoutParams() );
            fragmentContainer.addView( fragmentLayout );
            Log.e("a", "new fragment " + pantry);
            fm.beginTransaction()
                    .replace(fragmentLayout.getId(), ProductInstanceGroupBrowserFragment.newInstance( productID, pantry.getId() ) )
                    .addToBackStack(productID + pantry.getId())
                    .commit();
        }

        title.setText( pantry.getName() );
        // expandableLayout.setExpanded(isExpanded);
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