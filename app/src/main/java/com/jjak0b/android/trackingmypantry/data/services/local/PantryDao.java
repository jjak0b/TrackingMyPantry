package com.jjak0b.android.trackingmypantry.data.services.local;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstance;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@Dao
public interface PantryDao {

    @Query( "SELECT * FROM pantries" )
    List<Pantry> getAll();

    @Update(
            entity = ProductInstance.class,
            onConflict = OnConflictStrategy.IGNORE
    )
    void moveInstanceToPantry(ProductInstanceLocation... update);


    class ProductInstanceLocation {
        long id;
        long pantry_id;

        ProductInstanceLocation(long id, long pantry_id){
            this.id = id;
            this.pantry_id = pantry_id;
        }

        ProductInstanceLocation(@NotNull ProductInstance instance, @NotNull Pantry location){
            this.id = instance.getId();
            this.pantry_id = location.getId();
        }
    }
}
