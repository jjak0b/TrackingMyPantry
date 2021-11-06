package com.jjak0b.android.trackingmypantry.data.db.relationships;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;

import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;

@Entity(
        tableName = "purchaseInfoWithPlace"
)
public class PurchaseInfoWithPlace {
    @Embedded
    public PurchaseInfo info;

    @Relation(
            parentColumn = "place_id",
            entityColumn = "id"
    )
    public Place place;
}