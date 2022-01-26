package com.jjak0b.android.trackingmypantry.data.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.results.PantryDetails;

import java.util.List;

@Dao
public abstract class PantryDao {

    @Query("SELECT * FROM pantries WHERE pantry_id = :id AND owner_id = :owner_id")
    public abstract LiveData<Pantry> get(long id, String owner_id);

    @Query("SELECT * FROM pantries WHERE name = :name AND owner_id = :owner_id")
    public abstract LiveData<Pantry> get(String name, String owner_id);

    @Query("SELECT * FROM pantries WHERE owner_id = :owner_id")
    public abstract LiveData<List<Pantry>> getAll(String owner_id);

    @Insert(
            onConflict = OnConflictStrategy.IGNORE
    )
    public abstract long insert(Pantry pantry);

    @Update(
            onConflict = OnConflictStrategy.IGNORE
    )
    public abstract int update(Pantry pantry);

    @Delete
    public abstract void delete(Pantry pantry);

    @Query("SELECT * FROM productinstancegroup WHERE product_id = :productID AND pantry_id = :pantryID ORDER BY expiryDate, currentAmountPercent, quantity")
    public abstract LiveData<List<ProductInstanceGroup>> getContent(String productID, long pantryID);

    @Query("SELECT P.*, SUM(G.quantity) as totalQuantity FROM ( SELECT id, pantry_id, product_id, quantity FROM productinstancegroup ) AS G INNER JOIN pantries AS P ON G.pantry_id = P.pantry_id WHERE G.product_id = :productID GROUP BY G.pantry_id ORDER BY P.name"  )
    public abstract LiveData<List<PantryDetails>> getAllWithGroupsContaining(String productID);

}
