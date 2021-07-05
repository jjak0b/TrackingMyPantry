package com.jjak0b.android.trackingmypantry.data.model.relationships;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Junction;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity(
    tableName = "tagWithProducts"
)
class TagWithProducts {
    @Embedded
    public ProductTag tag;

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
            associateBy =  @Junction(
                    value = TagAndProduct.class,
                    parentColumn = "tag_id",
                    entityColumn = "product_id"
            )
    )
    public List<Product> products;
}

@Entity(
        tableName = "productWithTags"
)
class ProductWithTags {
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

@Entity(
        tableName = "assignedTags",
        primaryKeys = {"product_id", "tag_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "id",
                        childColumns = "product_id"
                ),
                @ForeignKey(
                        entity = ProductTag.class,
                        parentColumns = "id",
                        childColumns = "tag_id"
                )
        }
)
public class TagAndProduct {
    @NotNull
    @ColumnInfo(name = "product_id", index = true)
    public String fk_productId;

    @ColumnInfo(name = "tag_id", index = true )
    public int fk_tagId;
}