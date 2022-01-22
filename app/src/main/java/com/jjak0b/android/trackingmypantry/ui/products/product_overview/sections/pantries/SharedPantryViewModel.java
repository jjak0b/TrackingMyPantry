package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.pantries;

import android.app.Application;

import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.ui.util.ItemSourceViewModel;

public class SharedPantryViewModel extends ItemSourceViewModel<Pantry> {

    public SharedPantryViewModel(Application application) {
        super(application);
    }
}
