package com.jjak0b.android.trackingmypantry.data.db.relationships;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;

public class ProductInstanceGroupInfo {

    @Embedded
    public ProductInstanceGroup group;

    @Relation(
            parentColumn = "product_id",
            entityColumn = "id"
    )
    public UserProduct product;

    @Relation(
            parentColumn = "pantry_id",
            entityColumn = "pantry_id"
    )
    public Pantry pantry;
}
