package com.jjak0b.android.trackingmypantry.data.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.jjak0b.android.trackingmypantry.data.db.entities.Place;

import java.util.List;

@Dao
public abstract class PlaceDao {
    @Query("SELECT * FROM places" )
    public abstract LiveData<List<Place>> getAll();

    @Query("SELECT * FROM places WHERE id = :place_id" )
    public abstract LiveData<Place> getPlace(String place_id);

    @Query("SELECT * FROM places WHERE " +
            " LOWER(name) LIKE (:query) " +
            " OR LOWER(place) LIKE (:query) " +
            " OR LOWER(locality) LIKE (:query) " +
            " OR LOWER(neighborhood) LIKE (:query) " +
            " OR LOWER(postcode) LIKE (:query) " +
            " OR (LOWER(street) LIKE (:query) AND LOWER(houseNumber) LIKE (:query) )" +
            " OR LOWER(district) LIKE (:query)" +
            " OR LOWER(region) LIKE (:query)" +
            " OR LOWER(country) LIKE (:query) " +
            " ORDER BY name, place"
    )
    public abstract LiveData<List<Place>> search(String query);

    @Transaction
    public void insertPlace(Place place) {
        if( place == null ) return;
        int c = update(place);

        if( c <= 0) {
            insert(place);
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insert(Place place);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract int update(Place place);
}
