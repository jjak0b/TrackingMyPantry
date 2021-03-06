package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups.holder;

import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.ui.ItemViewHolder;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups.model.ProductInstanceGroupInteractionsListener;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries.products_groups.model.ProductInstanceGroupViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.QuantityPickerBuilder;


public class ProductInstanceGroupViewHolder extends ItemViewHolder<ProductInstanceGroupViewModel> {

    private TextInputEditText expireDate;
    private Chip badge;
    private LinearProgressIndicator totalAmountPercentBar;
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

        expireDate.setText(DateFormat
                .getDateFormat(itemView.getContext())
                .format(group.getExpiryDate())
        );
        badge.setText(String.valueOf(group.getQuantity()));
        totalAmountPercentBar.setProgressCompat(group.getCurrentAmountPercent(), true);

        if( group.getCurrentAmountPercent() <= 25 ){
            totalAmountPercentBar.setIndicatorColor(Color.RED);
        }
        else if( group.getCurrentAmountPercent() <= 50 ){
            totalAmountPercentBar.setIndicatorColor(Color.YELLOW);
        }
        else {
            totalAmountPercentBar.setIndicatorColor(Color.GREEN);
        }
        setupInteractions();
    }

    public static ProductInstanceGroupViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_products_groups_browser_list_item, parent, false);
        return new ProductInstanceGroupViewHolder(view);
    }

    private void setupInteractions(){
        setupConsume();
        setupRemove();
        setupMore();
    }

    private void setupConsume() {
        ProductInstanceGroup group = getViewModel().getItem();
        ProductInstanceGroupInteractionsListener listener = getViewModel().getInteractionsListener();

        consumeBtn.setClickable(true);
        consumeBtn.setOnClickListener(v -> {
            final int INDEX_AMOUNT = 0;
            int parameters[] = new int[] {
                    group.getCurrentAmountPercent()
            };

            View dialogView = LayoutInflater
                    .from(v.getContext()).inflate(R.layout.dialog_consume_amount, null);

            dialogView.findViewById(R.id.empty)
                    .setOnClickListener(v1 -> parameters[INDEX_AMOUNT] = 0);
            dialogView.findViewById(R.id.full)
                    .setOnClickListener(v1 -> parameters[INDEX_AMOUNT] = group.getCurrentAmountPercent() );
            Slider slider = dialogView.findViewById(R.id.amountPercentSlider);
            slider.setValueFrom(0);
            slider.setValueTo(parameters[INDEX_AMOUNT]);
            slider.setValue(parameters[INDEX_AMOUNT]);
            slider.addOnChangeListener((slider1, value, fromUser) -> {
                if( fromUser ){
                    parameters[INDEX_AMOUNT] = (int) value;
                }
            });

            new MaterialAlertDialogBuilder(itemView.getContext())
                    .setCancelable(true)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.option_consume, (dialog, which) -> {
                        listener.onConsume(
                                getBindingAdapterPosition(),
                                group,
                                group.getCurrentAmountPercent() - parameters[INDEX_AMOUNT]
                        );
                    })
                    .setView(dialogView)
                    .create()
                    .show();
        });
    }

    private void setupRemove() {
        ProductInstanceGroup group = getViewModel().getItem();
        ProductInstanceGroupInteractionsListener listener = getViewModel().getInteractionsListener();

        removeBtn.setClickable(true);
        removeBtn.setOnClickListener(v -> {
            new QuantityPickerBuilder(itemView.getContext())
                    .setMin(1)
                    .setMax(group.getQuantity())
                    .setPositiveButton(android.R.string.ok, quantity -> listener.onRemove(
                            getBindingAdapterPosition(),
                            group,
                            quantity
                    ))
                    .setNegativeButton(android.R.string.cancel , null )
                    .setCancelable(true)
                    .setTitle(R.string.product_quantity)
                    .show();
        });
    }

    private void setupMore(){
        ProductInstanceGroup group = getViewModel().getItem();
        ProductInstanceGroupInteractionsListener listener = getViewModel().getInteractionsListener();

        PopupMenu popup = new PopupMenu(itemView.getContext(), moreBtn);

        moreBtn.setOnClickListener(v -> {
            listener.onMore(
                    getBindingAdapterPosition(),
                    group,
                    popup
            );
        });
    }

}