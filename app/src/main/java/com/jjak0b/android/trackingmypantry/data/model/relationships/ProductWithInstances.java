package com.jjak0b.android.trackingmypantry.data.model.relationships;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.model.entities.Product;
import com.jjak0b.android.trackingmypantry.data.model.entities.ProductInstanceGroup;

import java.util.List;

@Entity(
    tableName = "productWithInstances"
)
public class ProductWithInstances {
    @Embedded
    public Product product;

    @Relation(
            parentColumn = "id",
            entityColumn = "product_id"
    )
    public List<ProductInstanceGroup> instances;
}
