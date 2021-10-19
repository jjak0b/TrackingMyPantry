package com.jjak0b.android.trackingmypantry.ui.util;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class SelectItemDialogBuilder<T> extends MaterialAlertDialogBuilder {

    public SelectItemDialogBuilder(@NonNull Context context) {
        super(context);
    }

    public SelectItemDialogBuilder(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }

    public SelectItemDialogBuilder loadOn(
            @NonNull LiveData<List<T>> liveData,
            @NonNull LifecycleOwner lifecycleOwner,
            @Nullable OnItemSubmitListener<T> submitListener) {

        ArrayAdapter<T> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.select_dialog_singlechoice);
        liveData.observe(lifecycleOwner, new Observer<List<T>>() {
            @Override
            public void onChanged(List<T> items) {
                adapter.clear();
                adapter.addAll(items);
                liveData.removeObserver(this::onChanged);
            }
        });

        super.setAdapter(adapter, (dialog, which) -> {
            if( submitListener != null )
                submitListener.onItemSubmit(adapter.getItem(which));
        });

        return this;
    }


    public interface OnItemSubmitListener<T> {
        void onItemSubmit( T item );
    }
}
