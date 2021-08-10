package com.jjak0b.android.trackingmypantry.ui.pantries;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.listener.SimpleTableViewListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jjak0b.android.trackingmypantry.ItemViewHolder;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_instance_group_table.ProductInstanceGroupTableViewAdapter;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;


public class PantryViewHolder extends ItemViewHolder<PantryViewModel> {

    private TextView title;
    private TextView description;
    private ImageView image;
    private Chip badge;
    private ChipGroup tags;
    private ExpandableLayout expandableLayout;
    private ImageButton actionExpandBtn;
    private ViewGroup tableContainer;
    private TableView tableView;
    private ProductInstanceGroupTableViewAdapter tableAdapter;

    public PantryViewHolder(@NonNull View itemView ) {
        super(itemView);
        title = itemView.findViewById(R.id.cardTitle);
        badge = itemView.findViewById(R.id.cardBadge);
        expandableLayout = itemView.findViewById(R.id.expandable_layout);
        actionExpandBtn = itemView.findViewById(R.id.actionExpandBtn);
        tableContainer = itemView.findViewById(R.id.tableContainer);
        tableView = itemView.findViewById(R.id.tableView);
        Log.e("a", "new instance " + this.toString());
    }

    @Override
    public void bindTo(PantryViewModel viewModel) {
        super.bindTo(viewModel);

        PantryWithProductInstanceGroups pantryWGroups = getViewModel().getItem();
        Log.e("a", "bind " + pantryWGroups + " to " + this.toString() );
        PantryInteractionsListener listener = getViewModel().getInteractionsListener();
        tableAdapter = new ProductInstanceGroupTableViewAdapter();
        Pantry pantry = pantryWGroups.pantry;
        List<ProductInstanceGroup> pantryItems = pantryWGroups.instances;

        bindTable(pantryItems, listener);

        title.setText( pantry.getName() );
        badge.setText( String.valueOf(pantryItems.size()) );
        expandableLayout.setExpanded(getViewModel().getExpanded());
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
                getViewModel().setExpanded(expandableLayout.isExpanded());
            }
        });
    }


    private void bindTable(List<ProductInstanceGroup> itemsList, PantryInteractionsListener listener){
        View view = itemView;

        // hide corner and column of row number
        tableView.setShowCornerView( false );
        // block focus when items are changed
        // prevent: java.lang.IllegalArgumentException: parameter must be a descendant of this view
        tableView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        tableView.setAdapter(tableAdapter);
        tableView.setTableViewListener(new SimpleTableViewListener() {
            @Override
            public void onCellClicked(@NonNull RecyclerView.ViewHolder cellView, int column, int row) {
                // tableView.setSelectedRow(row);

                listener.onItemClicked(
                        getBindingAdapterPosition(),
                        itemView,
                        row,
                        cellView.itemView,
                        tableAdapter
                );
            }

            @Override
            public void onCellLongPressed(@NonNull RecyclerView.ViewHolder cellView, int column, int row) {
                // tableView.setSelectedRow(row);
                listener.onItemLongClicked(
                        getBindingAdapterPosition(),
                        itemView,
                        row,
                        cellView.itemView,
                        tableAdapter
                );
            }
        });

        ViewGroup container = tableContainer;
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if( container.getWidth() > 0 ) {
                    container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    tableAdapter.setRowWidth(container.getWidth());

                    // Note: must not be empty: https://github.com/evrencoskun/TableView/issues/26
                    if( !itemsList.isEmpty() ) {
                        tableAdapter.submitList(itemsList);
                    }
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