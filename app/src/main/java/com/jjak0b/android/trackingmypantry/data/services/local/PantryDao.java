package com.jjak0b.android.trackingmypantry.data.services.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@Dao
public interface PantryDao {

    @Query( "SELECT * FROM pantries" )
    LiveData<List<Pantry>> getAll();

    @Update(
            entity = ProductInstanceGroup.class,
            onConflict = OnConflictStrategy.IGNORE
    )
    void moveInstanceToPantry(ProductInstanceLocation... update);

    @Insert(
            onConflict = OnConflictStrategy.IGNORE
    )
    ListenableFuture<Long> addPantry(Pantry... pantry);

    class ProductInstanceLocation {
        long id;
        long pantry_id;

        ProductInstanceLocation(long id, long pantry_id){
            this.id = id;
            this.pantry_id = pantry_id;
        }

        ProductInstanceLocation(@NotNull ProductInstanceGroup instance, @NotNull Pantry location){
            this.id = instance.getId();
            this.pantry_id = location.getId();
        }
    }
}
