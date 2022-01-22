package com.jjak0b.android.trackingmypantry.data.db.daos;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.results.PantryDetails;

import java.util.List;

@Dao
public abstract class PantryDao {

    @Query("SELECT * FROM pantries WHERE pantry_id = :id AND owner_id = :owner_id")
    public abstract LiveData<Pantry> get(long id, String owner_id);

    @Query("SELECT * FROM pantries WHERE owner_id = :owner_id")
    public abstract LiveData<List<Pantry>> getAll(String owner_id);

    @Update(
            entity = ProductInstanceGroup.class,
            onConflict = OnConflictStrategy.IGNORE
    )
    abstract void moveInstanceToPantry(ProductInstanceLocation... update);

    @Insert(
            onConflict = OnConflictStrategy.IGNORE
    )
    public abstract ListenableFuture<Long> addPantry(Pantry pantry);

    @Insert(
            onConflict = OnConflictStrategy.IGNORE
    )
    public abstract long insert(Pantry pantry);

    @Update(
            onConflict = OnConflictStrategy.IGNORE
    )
    public abstract int update(Pantry pantry);

    @Query("SELECT * FROM productinstancegroup WHERE product_id = :productID AND pantry_id = :pantryID")
    public abstract LiveData<List<ProductInstanceGroup>> getContent(String productID, long pantryID);

    @Query("SELECT P.*, SUM(G.quantity) as totalQuantity FROM ( SELECT id, pantry_id, product_id, quantity FROM productinstancegroup ) AS G INNER JOIN pantries AS P ON G.pantry_id = P.pantry_id WHERE G.product_id = :productID GROUP BY G.pantry_id ORDER BY P.name"  )
    public abstract LiveData<List<PantryDetails>> getAllWithGroupsContaining(String productID);

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
