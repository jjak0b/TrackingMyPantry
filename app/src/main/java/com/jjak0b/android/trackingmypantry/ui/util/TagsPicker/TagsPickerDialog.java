package com.jjak0b.android.trackingmypantry.ui.util.TagsPicker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.ui.util.MultiSelectItemsFragmentDialog;
import com.jjak0b.android.trackingmypantry.ui.util.MultiSelectItemsViewModel;

import java.util.List;

public class TagsPickerDialog extends MultiSelectItemsFragmentDialog<ProductTag> {

    private static final String TAG = "TagsPickerDialog";
    private TagsPickerViewModel mViewModel;
    public SharedTagsViewModel mSharedViewModel;

    @Override
    public MultiSelectItemsViewModel<ProductTag> initViewModel() {
        mViewModel = new ViewModelProvider(this).get(TagsPickerViewModel.class);
        return mViewModel;
    }

    public TagsPickerViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedViewModel = new ViewModelProvider(requireActivity()).get(SharedTagsViewModel.class);

        mSharedViewModel.getItem().observe(TagsPickerDialog.this, new Observer<Resource<List<ProductTag>>>() {
            @Override
            public void onChanged(Resource<List<ProductTag>> resource) {
                Log.d(TAG, "Init already selected items once if any: "+ resource.getData() );
                if( resource.getData() != null ) {
                    mViewModel.setItems(resource.getData());
                }
                mSharedViewModel.getItem().removeObserver(this);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel.getAllItems().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                default:
                    List<ProductTag> items = resource.getData();

                    if( items == null || items.isEmpty() ) {
                        Toast.makeText(requireContext(), R.string.no_tags_available, Toast.LENGTH_LONG)
                                .show();
                        dismiss();
                    }
                    break;
            }
        });
    }

    @Override
    public void onSubmit(LiveData<Resource<List<ProductTag>>> source) {
        mSharedViewModel.setItemSource(source);
    }

}
