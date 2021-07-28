package com.jjak0b.android.trackingmypantry.data.model.relationships;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;

import java.util.List;

public class ProductInstancesInPantry {
    @Embedded
    public Pantry pantry;

    @Relation(
            parentColumn = "id",
            entityColumn = "pantry_id"
    )
    public List<ProductInstanceGroup> instances;
}
