package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.edit;

import android.app.Application;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.ui.products.product_overview.ProductDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ImageUtil;

public class _EditProductDetailsViewModel extends ProductDetailsViewModel {

    public _EditProductDetailsViewModel(Application application) {
        super(application);
    }

    public ListenableFuture<Void> submit() {

        ProductWithTags old = getProduct().getValue();
        ProductWithTags productWithTags = new ProductWithTags();

        Product.Builder builder = new Product.Builder().from(old != null ? old.product : null)
                .setName(getName().getValue())
                .setDescription(getDescription().getValue())
                .setBarcode(getBarcode().getValue());

        if( getImage().getValue() != null ) {
            builder.setImg(ImageUtil.convert(getImage().getValue()));
        }

        productWithTags.product = builder.build();
        productWithTags.tags = getAssignedTags().getValue();

        return pantryRepository.updateProductLocal(productWithTags);
    }

}
