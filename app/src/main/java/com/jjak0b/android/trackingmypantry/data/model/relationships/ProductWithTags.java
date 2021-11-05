package com.jjak0b.android.trackingmypantry.data.model.relationships;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Junction;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.model.entities.Product;
import com.jjak0b.android.trackingmypantry.data.model.entities.ProductTag;

import java.util.List;

@Entity(
        tableName = "productWithTags"
)
public class ProductWithTags {
    @Embedded
    public Product product;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy =  @Junction(
                    value = TagAndProduct.class,
                    parentColumn = "product_id",
                    entityColumn = "tag_id"
            )
    )
    public List<ProductTag> tags;
}
