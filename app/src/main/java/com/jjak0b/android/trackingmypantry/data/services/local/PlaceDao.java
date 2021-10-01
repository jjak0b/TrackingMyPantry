package com.jjak0b.android.trackingmypantry.data.services.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.model.Place;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithInstances;

import java.util.List;

@Dao
public interface PlaceDao {
    @Query("SELECT * FROM places" )
    LiveData<List<Place>> getAll();

    @Query("SELECT * FROM places WHERE id = :place_id" )
    LiveData<Place> getPlace(String place_id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    ListenableFuture<Void> insertPlace(Place place);
}
