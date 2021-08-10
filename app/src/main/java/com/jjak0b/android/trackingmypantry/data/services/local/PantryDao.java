package com.jjak0b.android.trackingmypantry.data.services.local;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Dao
public abstract class PantryDao {

    @Query( "SELECT * FROM pantries" )
    public abstract LiveData<List<Pantry>> getAll();

    @Update(
            entity = ProductInstanceGroup.class,
            onConflict = OnConflictStrategy.IGNORE
    )
    abstract void moveInstanceToPantry(ProductInstanceLocation... update);

    @Insert(
            onConflict = OnConflictStrategy.REPLACE
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

        ProductInstanceLocation(@NotNull ProductInstanceGroup instance, @NotNull Pantry location){
            this.id = instance.getId();
            this.pantry_id = location.getId();
        }
    }
}
