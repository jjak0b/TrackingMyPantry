package com.jjak0b.android.trackingmypantry.ui.util.TagsPicker;

import android.app.Application;

import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.ui.util.ItemSourceViewModel;

import java.util.List;

public class SharedTagsViewModel extends ItemSourceViewModel<List<ProductTag>> {
    public SharedTagsViewModel(Application application) {
        super(application);
    }
}
