package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.chip.Chip;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.results.PantryDetails;
import com.jjak0b.android.trackingmypantry.ui.ItemViewHolder;


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

        PantryDetails pantryWGroups = getViewModel().getItem();
        PantryInteractionsListener listener = getViewModel().getInteractionsListener();
        Pantry pantry = pantryWGroups.pantry;

        title.setText( pantry.getName() );
        badge.setText( String.valueOf(pantryWGroups.totalQuantity) );
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