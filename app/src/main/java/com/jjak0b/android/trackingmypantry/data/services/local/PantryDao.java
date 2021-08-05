package com.jjak0b.android.trackingmypantry.data.services.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PantryWithProductInstanceGroups;

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
            onConflict = OnConflictStrategy.REPLACE
    )
    ListenableFuture<Long> addPantry(Pantry pantry);

    @Transaction
    @Query( "SELECT * FROM pantries AS P1 INNER JOIN ( SELECT DISTINCT P2.pantry_id FROM productinstancegroup AS G INNER JOIN pantries AS P2 ON G.pantry_id = P2.pantry_id WHERE product_id = (:productID) ) AS IDS ON P1.pantry_id = IDS.pantry_id ORDER BY P1.name")
    LiveData<List<PantryWithProductInstanceGroups>> getAllThatContains(String productID );

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
