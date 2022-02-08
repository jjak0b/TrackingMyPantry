package com.jjak0b.android.trackingmypantry.data.db.daos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.results.ExpirationInfo;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Dao
public abstract class ProductInstanceDao {

    @Query("SELECT * FROM productinstancegroup WHERE id = :group_id" )
    public abstract LiveData<ProductInstanceGroup> getGroup(long group_id);

    @Query("SELECT G.product_id, G.pantry_id, G.expiryDate, PR.p_name AS product_name, P.name AS pantry_name, SUM(G.quantity) as quantity" +
            " FROM pantries AS P INNER JOIN  productinstancegroup AS G INNER JOIN userproducts AS PR" +
            " ON P.pantry_id = G.pantry_id AND PR.product_id = G.product_id" +
            " WHERE (" +
                " ( :userOwnerID IS NULL OR P.owner_id = :userOwnerID )" +
                " AND (:productID IS NULL OR G.product_id = :productID )" +
                " AND (:pantryID IS NULL OR G.pantry_id = :pantryID )" +
                " AND (:expiryDate IS NULL OR G.expiryDate = :expiryDate ) )" +
            " GROUP BY G.product_id, G.pantry_id, G.expiryDate ORDER BY G.expiryDate"
    )
    public abstract List<ExpirationInfo> getInfoOfAll(@NonNull String userOwnerID, @Nullable String productID, @Nullable Long pantryID, @Nullable Date expiryDate);

    /**
     * Get first entry that match with the same product ID and same pantry ID and others params
     */
    @Query( "SELECT * FROM productinstancegroup WHERE owner_id =:ownerID AND product_id = :productID AND pantry_id = :pantryID AND currentAmountPercent = :currentAmountPercent AND expiryDate = :expiryDate")
    public abstract ProductInstanceGroup getFirstMatchingGroup( String ownerID, String productID, long pantryID, int currentAmountPercent, Date expiryDate );

    /**
     * Add the group but first detect if it has to be merged with another one, or update it self
     * @param group
     * @return
     */
    @Transaction
    public long merge(ProductInstanceGroup group) {
        ProductInstanceGroup match = getFirstMatchingGroup(
                group.getUserId(),
                group.getProductId(),
                group.getPantryId(),
                group.getCurrentAmountPercent(),
                group.getExpiryDate()
        );

        if( match != null && !Objects.equals(match.getId(), group.getId()) ) {
            // merge and update merged
            match.setQuantity(match.getQuantity()+group.getQuantity());
            update(match);
            // delete entry because we just virtually merged it
            if( group.getId() > 0 )
                delete(group);
            return match.getId();
        }
        // match and group are the same
        else if( group.getId() > 0 ) {
            update(group);
            return group.getId();
        }
        // no matches, then add it
        else {
            return insert(group);
        }
    }

    @Insert
    abstract long insert(ProductInstanceGroup group);

    @Update
    abstract int update(ProductInstanceGroup instance);

    @Delete
    abstract void delete(ProductInstanceGroup group);

    @Delete
    public abstract void deleteAll(ProductInstanceGroup... instances);

    @Update
    public abstract int updateAll(ProductInstanceGroup... instances);
}
