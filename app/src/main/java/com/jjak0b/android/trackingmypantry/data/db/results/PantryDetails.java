package com.jjak0b.android.trackingmypantry.data.db.results;

import androidx.room.Embedded;

import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;

import java.util.Objects;

public class PantryDetails {
    @Embedded
    public Pantry pantry;

    public int totalQuantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PantryDetails that = (PantryDetails) o;
        return totalQuantity == that.totalQuantity && Objects.equals(pantry, that.pantry);
    }
}
