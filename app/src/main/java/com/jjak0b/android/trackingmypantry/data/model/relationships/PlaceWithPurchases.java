package com.jjak0b.android.trackingmypantry.data.model.relationships;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;


import com.jjak0b.android.trackingmypantry.data.model.Place;
import com.jjak0b.android.trackingmypantry.data.model.PurchaseInfo;


import java.util.List;

@Entity(
        tableName = "placeWithPurchases"
)
public class PlaceWithPurchases {
    @Embedded
    public Place place;

    @Relation(
            entity = PurchaseInfo.class,
            parentColumn = "id",
            entityColumn = "place_id"
    )
    public List<PurchaseInfo> purchases;
}
