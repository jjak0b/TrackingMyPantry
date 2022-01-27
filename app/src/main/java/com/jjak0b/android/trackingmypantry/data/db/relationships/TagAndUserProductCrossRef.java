package com.jjak0b.android.trackingmypantry.data.db.relationships;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;

@Entity(
        tableName = "assignedTags",
        primaryKeys = {"product_id", "owner_id", "tag_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserProduct.class,
                        parentColumns = { "product_id", "owner_id" },
                        childColumns = { "product_id", "owner_id" },
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ProductTag.class,
                        parentColumns = "id",
                        childColumns = "tag_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                )
        }
)
public class TagAndUserProductCrossRef {
    @NonNull
    @ColumnInfo(name = "product_id", index = true)
    public String fk_productId;

    @NonNull
    @ColumnInfo(name = "owner_id", index = true)
    public String fk_userId;

    @ColumnInfo(name = "tag_id", index = true )
    public long fk_tagId;

    public TagAndUserProductCrossRef(@NonNull String fk_productId, @NonNull String fk_userId, long fk_tagId) {
        this.fk_productId = fk_productId;
        this.fk_userId = fk_userId;
        this.fk_tagId = fk_tagId;
    }

    @Override
    public String toString() {
        return "TagAndUserProductCrossRef{" +
                "fk_productId='" + fk_productId + '\'' +
                ", fk_userId='" + fk_userId + '\'' +
                ", fk_tagId=" + fk_tagId +
                '}';
    }
}