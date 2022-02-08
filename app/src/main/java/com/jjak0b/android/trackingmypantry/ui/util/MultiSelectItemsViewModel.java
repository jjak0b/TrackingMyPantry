package com.jjak0b.android.trackingmypantry.ui.util;

import android.app.Application;
import android.util.SparseBooleanArray;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class MultiSelectItemsViewModel<I> extends AndroidViewModel {

    private AppExecutors appExecutors;

    private MediatorLiveData<Resource<List<I>>> mItems;
    private LiveData<Resource<List<I>>> mDefaultItems;
    private MutableLiveData<Resource<SparseBooleanArray>> mItemsSource;

    public MultiSelectItemsViewModel(Application application) {
        super(application);
        appExecutors = AppExecutors.getInstance();

        mItemsSource = new MutableLiveData<>(Resource.loading(null));
        mDefaultItems = new MutableLiveData<>(Resource.success(new ArrayList<>(0)));

        mItems = new MediatorLiveData<>();
        mItems.addSource(
                Transformations.forward(getSparseItems(), resource -> {
                    return getListItems(resource.getData());
                }),
                mItems::setValue
        );


        setItems((SparseBooleanArray) null);
    }

    public abstract LiveData<Resource<List<I>>> getAllItems();

    public LiveData<Resource<SparseBooleanArray>> getSparseItems() {
        return mItemsSource;
    }

    public LiveData<Resource<List<I>>> getListItems() {
        return mItems;
    }

    public LiveData<Resource<List<I>>> getListItems(SparseBooleanArray checkedItems) {
        return Transformations.forward(getAllItems(), resource -> {
            List<I> list = resource.getData();
            if( checkedItems != null && list != null && checkedItems.size() > 0 && list.size() > 0 ) {
                return Transformations.simulateApi(appExecutors.networkIO(), appExecutors.mainThread(), () -> {
                    ArrayList<I> checkedTags = new ArrayList<>((int)Math.floor(Math.sqrt(list.size())));
                    for (int i = 0; i < checkedItems.size(); i++) {
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
        if(!Objects.equals(checkedItems, this.mItemsSource.getValue().getData() )){
            this.mItemsSource.setValue(Resource.success(checkedItems));
        }
    }

    public void setItems(List<I> items ) {

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

        mItems.addSource(source, itemsResource -> {
            if( itemsResource.getStatus() == Status.LOADING ) return;
            mItems.removeSource(source);
            setItems(itemsResource.getData());
        });
    }
}