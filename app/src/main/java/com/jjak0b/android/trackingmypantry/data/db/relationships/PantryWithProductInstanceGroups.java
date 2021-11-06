package com.jjak0b.android.trackingmypantry.data.db.relationships;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PantryWithProductInstanceGroups that = (PantryWithProductInstanceGroups) o;
        return Objects.equals(pantry, that.pantry) &&
                Objects.equals(instances, that.instances);
    }

}
