package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.google.android.material.chip.Chip;
import com.jjak0b.android.trackingmypantry.ui.ItemViewHolder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;

import java.util.List;


public class PantryViewHolder extends ItemViewHolder<PantryViewModel> {

    private TextView title;
    private Chip badge;

    public PantryViewHolder(@NonNull View itemView ) {
        super(itemView);
        title = itemView.findViewById(R.id.cardTitle);
        badge = itemView.findViewById(R.id.cardBadge);
    }

    @Override
    public void bindTo(PantryViewModel viewModel) {
        super.bindTo(viewModel);

        PantryWithProductInstanceGroups pantryWGroups = getViewModel().getItem();
        PantryInteractionsListener listener = getViewModel().getInteractionsListener();
        Pantry pantry = pantryWGroups.pantry;
        List<ProductInstanceGroup> pantryItems = pantryWGroups.instances;


        title.setText( pantry.getName() );
        badge.setText( String.valueOf(pantryItems.size()) );
        itemView.setClickable(true);
        itemView.setOnClickListener(v -> {
            listener.onItemClicked(
                    getBindingAdapterPosition(),
                    itemView,
                    pantryWGroups
            );
        });
    }

    static PantryViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_pantries_browser_list_item, parent, false);
        return new PantryViewHolder(view);
    }

}