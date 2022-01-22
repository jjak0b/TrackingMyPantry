package com.jjak0b.android.trackingmypantry.ui.register_product;

import android.app.Application;

import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.ui.util.ItemSourceViewModel;

public class SharedProductViewModel extends ItemSourceViewModel<Product> {

    public SharedProductViewModel(Application application) {
        super(application);
    }
}
