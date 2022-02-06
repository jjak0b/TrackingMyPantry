package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.app.Application;

import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductInstanceGroupInfo;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductInstanceDetailsViewModel;

public class SectionProductInstanceDetailsViewModel extends ProductInstanceDetailsViewModel {

    public SectionProductInstanceDetailsViewModel(@NonNull Application application) {
        super(application);
    }

    public void setDetails(ProductInstanceGroupInfo details ) {
        if( details != null ) {
            setPantry(details.pantry);
            setExpireDate(details.group.getExpiryDate());
            setQuantity(details.group.getQuantity());
        }
    }
}
