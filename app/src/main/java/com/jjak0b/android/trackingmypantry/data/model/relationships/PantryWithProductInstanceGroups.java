package com.jjak0b.android.trackingmypantry.data.model.relationships;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;

import java.util.List;

@Entity(
        tableName = "pantryWithProductInstanceGroups"
)
public class PantryWithProductInstanceGroups {
    @Embedded
    public Pantry pantry;

    @Relation(
            entity = ProductInstanceGroup.class,
            parentColumn = "pantry_id",
            entityColumn = "pantry_id"
    )
    public List<ProductInstanceGroup> instances;
}
