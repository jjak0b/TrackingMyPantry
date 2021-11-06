package com.jjak0b.android.trackingmypantry.data.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.relationships.PantryWithProductInstanceGroups;

import androidx.annotation.NonNull;

import java.util.List;

@Dao
public abstract class PantryDao {

    @Query( "SELECT * FROM pantries WHERE pantry_id = :id" )
    public abstract LiveData<Pantry> get(long id);

    @Query( "SELECT * FROM pantries" )
    public abstract LiveData<List<Pantry>> getAll();

    @Update(
            entity = ProductInstanceGroup.class,
            onConflict = OnConflictStrategy.IGNORE
    )
    abstract void moveInstanceToPantry(ProductInstanceLocation... update);

    @Insert(
            onConflict = OnConflictStrategy.IGNORE
    )
    public abstract ListenableFuture<Long> addPantry(Pantry pantry);

    @Query( "SELECT * FROM pantries AS P1 INNER JOIN ( SELECT DISTINCT P2.pantry_id FROM productinstancegroup AS G INNER JOIN pantries AS P2 ON G.pantry_id = P2.pantry_id WHERE product_id = (:productID) ) AS IDS ON P1.pantry_id = IDS.pantry_id ORDER BY P1.name ")
    public abstract LiveData<List<Pantry>> getAllThatContains(String productID);

    @Query("SELECT * FROM productinstancegroup WHERE product_id = :productID AND pantry_id = :pantryID" )
    abstract List<ProductInstanceGroup> getAllInstancesOfProduct(String productID, long pantryID);

    @Query("SELECT P.*, G.* FROM productinstancegroup AS G INNER JOIN pantries AS P ON G.pantry_id = P.pantry_id WHERE product_id IS :productID")
    public abstract LiveData<List<PantryWithProductInstanceGroups>> getAllContaining(String productID);

    class ProductInstanceLocation {
        long id;
        long pantry_id;

        ProductInstanceLocation(long id, long pantry_id){
            this.id = id;
            this.pantry_id = pantry_id;
        }

        ProductInstanceLocation(@NonNull ProductInstanceGroup instance, @NonNull Pantry location){
            this.id = instance.getId();
            this.pantry_id = location.getId();
        }
    }
}
