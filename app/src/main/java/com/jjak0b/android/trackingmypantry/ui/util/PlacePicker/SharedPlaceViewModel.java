package com.jjak0b.android.trackingmypantry.ui.util.PlacePicker;

import android.app.Application;

import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.ui.util.ItemSourceViewModel;

public class SharedPlaceViewModel extends ItemSourceViewModel<Place> {
    public SharedPlaceViewModel(Application application) {
        super(application);
    }
}
