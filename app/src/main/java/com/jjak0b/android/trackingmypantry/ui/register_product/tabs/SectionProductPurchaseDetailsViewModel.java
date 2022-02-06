package com.jjak0b.android.trackingmypantry.ui.register_product.tabs;

import android.app.Application;

import androidx.annotation.NonNull;

import com.jjak0b.android.trackingmypantry.data.db.relationships.PurchaseInfoWithPlace;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductPurchaseDetailsViewModel;

public class SectionProductPurchaseDetailsViewModel extends ProductPurchaseDetailsViewModel {
    public SectionProductPurchaseDetailsViewModel(@NonNull Application application) {
        super(application);
    }

    public void setDetails(PurchaseInfoWithPlace details) {
        if( details != null ) {
            setPurchasePlace(details.place);
            setCost(details.info.getCost());
            setPurchaseDate(details.info.getPurchaseDate());
        }
    }
}
