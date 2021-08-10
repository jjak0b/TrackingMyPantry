package com.jjak0b.android.trackingmypantry;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

public class ItemViewHolder<VM extends ViewModel & ItemViewModel<?>> extends RecyclerView.ViewHolder {
    protected VM viewModel;

    public ItemViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public VM getViewModel() {
        return viewModel;
    }

    public void setViewModel(VM viewModel) {
        this.viewModel = viewModel;
    }

    public void bindTo(VM viewModel) {
        setViewModel(viewModel);
    }
}
