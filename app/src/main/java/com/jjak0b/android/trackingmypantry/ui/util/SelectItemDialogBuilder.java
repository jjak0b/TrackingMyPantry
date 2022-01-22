package com.jjak0b.android.trackingmypantry.ui.util;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jjak0b.android.trackingmypantry.data.api.Resource;

import java.util.List;

public class SelectItemDialogBuilder<T> extends MaterialAlertDialogBuilder {

    public SelectItemDialogBuilder(@NonNull Context context) {
        super(context);
    }

    public SelectItemDialogBuilder<T> loadOn(
            @NonNull LiveData<Resource<List<T>>> liveData,
            @NonNull LifecycleOwner lifecycleOwner,
            @Nullable OnItemSubmitListener<T> submitListener) {

        ArrayAdapter<T> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.select_dialog_singlechoice);
        liveData.observe(lifecycleOwner, new Observer<Resource<List<T>>>() {
            @Override
            public void onChanged(Resource<List<T>> resource) {
                switch (resource.getStatus()) {
                    case LOADING:
                        adapter.clear();
                        break;
                    case SUCCESS:
                        adapter.addAll(resource.getData());
                        liveData.removeObserver(this::onChanged);
                        break;
                    case ERROR:
                        break;
                }
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
