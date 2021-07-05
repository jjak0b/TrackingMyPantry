package com.jjak0b.android.trackingmypantry.data.model.relationships;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Junction;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstance;

import java.util.List;

public class ProductInstancesInPantry {
    @Embedded
    public Pantry pantry;

    @Relation(
            parentColumn = "id",
            entityColumn = "pantry_id"
    )
    public List<ProductInstance> instances;
}
