package com.jjak0b.android.trackingmypantry.ui.products.product_overview.sections.edit;

import android.app.Application;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.ui.products.details.ProductDetailsViewModel;
import com.jjak0b.android.trackingmypantry.ui.util.ImageUtil;

public class EditProductDetailsViewModel extends ProductDetailsViewModel {

    public EditProductDetailsViewModel(Application application) {
        super(application);
    }

    public ListenableFuture<Void> submit() {

        Product old = getProduct().getValue();
        ProductWithTags productWithTags = new ProductWithTags();

        Product.Builder builder = new Product.Builder().from(old != null ? old : null)
                .setName(getName().getValue().getData())
                .setDescription(getDescription().getValue().getData())
                .setBarcode(getBarcode().getValue().getData());

        if( getImage().getValue() != null ) {
            builder.setImg(ImageUtil.convert(getImage().getValue().getData()));
        }

        productWithTags.product = builder.build();
        productWithTags.tags = getAssignedTags().getValue();

        return pantryRepository.updateProductLocal(productWithTags);
    }

}
