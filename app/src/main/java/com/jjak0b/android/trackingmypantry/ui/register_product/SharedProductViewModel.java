package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.app.Application;

import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.ui.util.ItemSourceViewModel;

public class SharedProductViewModel extends ItemSourceViewModel<UserProduct> {

    public SharedProductViewModel(Application application) {
        super(application);
    }
}
