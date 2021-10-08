package com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups.holder;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.google.android.material.chip.Chip;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.ItemViewHolder;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups.model.ProductInstanceGroupInteractionsListener;
import com.jjak0b.android.trackingmypantry.ui.pantries.product_overview.sections.pantries.products_groups.model.ProductInstanceGroupViewModel;


public class ProductInstanceGroupViewHolder extends ItemViewHolder<ProductInstanceGroupViewModel> {

    private TextInputEditText expireDate;
    private Chip badge;
    private Slider totalAmountPercentBar;
    private ImageButton moreBtn;
    private ImageButton consumeBtn;
    private ImageButton removeBtn;

    public ProductInstanceGroupViewHolder(@NonNull View itemView ) {
        super(itemView);
        badge = itemView.findViewById(R.id.chipQuantity);
        expireDate = itemView.findViewById(R.id.expireDateInput);
        moreBtn = itemView.findViewById(R.id.moreBtn);
        consumeBtn = itemView.findViewById(R.id.consumeBtn);
        removeBtn = itemView.findViewById(R.id.removeBtn);
        totalAmountPercentBar = itemView.findViewById(R.id.amountPercentBar);
    }

    @Override
    public void bindTo(ProductInstanceGroupViewModel viewModel) {
        super.bindTo(viewModel);

        ProductInstanceGroup group = getViewModel().getItem();
        ProductInstanceGroupInteractionsListener listener = getViewModel().getInteractionsListener();

        expireDate.setText(DateFormat
                .getDateFormat(itemView.getContext())
                .format(group.getExpiryDate())
        );
        badge.setText(String.valueOf(group.getQuantity()));
        totalAmountPercentBar.setValue(group.getCurrentAmountPercent());
    }

    public static ProductInstanceGroupViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_products_groups_browser_list_item, parent, false);
        return new ProductInstanceGroupViewHolder(view);
    }

}