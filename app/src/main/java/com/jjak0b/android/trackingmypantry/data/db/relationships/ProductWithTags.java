package com.jjak0b.android.trackingmypantry.data.db.relationships;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Junction;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;

import java.util.List;

@Entity(
        tableName = "productWithTags"
)
public class ProductWithTags {

    @PrimaryKey
    @Embedded
    public UserProduct product;

    @Relation(

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
