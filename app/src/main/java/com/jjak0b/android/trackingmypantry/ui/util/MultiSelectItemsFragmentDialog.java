package com.jjak0b.android.trackingmypantry.ui.util;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jjak0b.android.trackingmypantry.data.api.Resource;

import java.util.List;

public abstract class MultiSelectItemsFragmentDialog<T> extends DialogFragment {

    private MultiSelectItemsViewModel<T> mViewModel;
    private ArrayAdapter<T> adapter;
    private AlertDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_multiple_choice);
        mViewModel = this.initViewModel();
    }

    public abstract MultiSelectItemsViewModel<T> initViewModel();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog newDialog = new MaterialAlertDialogBuilder(requireContext())
                .setAdapter(adapter, null)
                .setPositiveButton(android.R.string.ok, this::onSubmit )
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        newDialog.getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        this.dialog = newDialog;
        return newDialog;
    }

    @NonNull
    @Override
    public AlertDialog getDialog() {
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel.getAllItems().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    adapter.clear();
                    break;
                case SUCCESS:
                    List<T> items = resource.getData();
                    adapter.addAll(items);
                    adapter.notifyDataSetChanged();
                    break;
                case ERROR:
                    break;
            }
        });

        mViewModel.getSparseItems().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    unselectAll();
                    break;
                case SUCCESS:
                    selectAll(resource.getData());
                    break;
                case ERROR:
                    unselectAll();
                    break;
            }
        });
    }

    protected void onSubmit(DialogInterface dialog, int which) {
        SparseBooleanArray checkedItems = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
        mViewModel.setItems(checkedItems);

        this.onSubmit(mViewModel.getListItems());
    }

    public abstract void onSubmit(LiveData<Resource<List<T>>> source);

    public void unselectAll() {
        final AlertDialog dialog = (AlertDialog) getDialog();
        ListView listView = dialog.getListView();
        for (int i = 0; i < listView.getCount(); i++) {
            listView.setItemChecked(i, false);
        }
    }

    public void selectAll(SparseBooleanArray checkedItems) {
        final AlertDialog dialog = (AlertDialog) getDialog();
        ListView listView = dialog.getListView();

        for (int i = 0; i < checkedItems.size(); i++) {
            int position = checkedItems.keyAt(i);
            boolean isChecked = checkedItems.get(position);
            listView.setItemChecked(position, isChecked );
        }
    }
}