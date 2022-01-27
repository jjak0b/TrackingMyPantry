package com.jjak0b.android.trackingmypantry.data.db.relationships;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;

import java.util.List;

public class ProductWithTags {

    @Embedded
    public UserProduct product;

    @Relation(
            entity = ProductTag.class,
            parentColumn = "product_id",
            entityColumn = "id",
            associateBy =  @Junction(
                    value = TagAndUserProductCrossRef.class,
                    parentColumn = "product_id",
                    entityColumn = "tag_id"
            )
    )
    public List<ProductTag> tags;

    @Override
    public String toString() {
        return "ProductWithTags{" +
                "product=" + product +
                ", tags=" + tags +
                '}';
    }
}
