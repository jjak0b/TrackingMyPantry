package com.jjak0b.android.trackingmypantry.data.model.relationships;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;

import java.util.List;

public class ProductInstanceGroupInfo {

    @Embedded
    public ProductInstanceGroup group;

    @Relation(
            parentColumn = "product_id",
            entityColumn = "id"
    )
    public Product product;

    @Relation(
            parentColumn = "pantry_id",
            entityColumn = "pantry_id"
    )
    public Pantry pantry;
}
