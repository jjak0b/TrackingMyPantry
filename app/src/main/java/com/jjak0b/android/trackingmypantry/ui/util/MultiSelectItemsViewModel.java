package com.jjak0b.android.trackingmypantry.ui.util;

import android.app.Application;
import android.util.SparseBooleanArray;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiSelectItemsViewModel<I> extends ItemSourceViewModel<SparseBooleanArray> {

    private static final String TAG = "MultiSelectItemsViewModel";
    private AppExecutors appExecutors;

    private MediatorLiveData<Resource<List<I>>> mItems;
    private LiveData<Resource<List<I>>> mDefaultItems = new MutableLiveData<>(Resource.success(new ArrayList<>(0)));

    public MultiSelectItemsViewModel(Application application) {
        super(application);
        appExecutors = AppExecutors.getInstance();

        mItems = new MediatorLiveData<>();
        // keeps the list of real items updated
        mItems.addSource(
                Transformations.forward(getSparseItems(), resource -> {
                    return getListItems(resource.getData());
                }),
                mItems::setValue
        );

    }

    public abstract LiveData<Resource<List<I>>> getAllItems();

    public LiveData<Resource<SparseBooleanArray>> getSparseItems() {
        return getItem();
    }

    public LiveData<Resource<List<I>>> getListItems() {
        return mItems;
    }

    public LiveData<Resource<List<I>>> getListItems(SparseBooleanArray checkedItems) {
        return Transformations.forward(getAllItems(), resource -> {
            List<I> list = resource.getData();
            if( checkedItems != null && list != null && checkedItems.size() > 0 && list.size() > 0 ) {
                int checkedSize = checkedItems.size();
                return Transformations.simulateApi(appExecutors.networkIO(), appExecutors.mainThread(), () -> {
                    ArrayList<I> checkedTags = new ArrayList<>((int)Math.floor(Math.sqrt(checkedSize)));
                    for (int i = 0; i < checkedSize; i++) {
                        int position = checkedItems.keyAt(i);
                        boolean isChecked = checkedItems.get(position);
                        if (isChecked) {
                            checkedTags.add(list.get(position));
                        }
                    }
                    return checkedTags;
                });
            }
            else {
                return mDefaultItems;
            }
        });
    }

    public void setItems(SparseBooleanArray checkedItems) {
        setItemSource(new MutableLiveData<>(Resource.success(checkedItems)));
    }

    public void setItems(List<I> items ) {

        // create a new items source from this list
        LiveData<Resource<SparseBooleanArray>> source
                = Transformations.forwardOnce(getAllItems(), resource -> {
            List<I> allItems = resource.getData();
            return Transformations.simulateApi(appExecutors.diskIO(), appExecutors.mainThread(), () -> {
                SparseBooleanArray checkedItems = new SparseBooleanArray(items.size());
                for (I item : items ) {
                    int position = allItems.indexOf(item);
                    if( position >= 0 ) checkedItems.put(position, true);
                }
                return checkedItems;
            });
        });

        setItemSource(source);
    }
}