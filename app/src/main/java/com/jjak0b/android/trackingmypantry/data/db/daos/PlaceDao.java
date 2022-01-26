package com.jjak0b.android.trackingmypantry.data.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jjak0b.android.trackingmypantry.data.db.entities.Place;

import java.util.List;

@Dao
public interface PlaceDao {
    @Query("SELECT * FROM places" )
    LiveData<List<Place>> getAll();

    @Query("SELECT * FROM places WHERE id = :place_id" )
    LiveData<Place> getPlace(String place_id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPlace(Place place);
}
