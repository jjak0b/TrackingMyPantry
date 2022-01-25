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
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductInstanceGroupInfo;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithInstances;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Dao
public abstract class ProductInstanceDao {


    @Transaction
    @Query("SELECT * FROM products" )
    public abstract List<ProductWithInstances> getAllInstancesOfProduct();

    @Query("SELECT * FROM productinstancegroup WHERE id = :group_id" )
    public abstract LiveData<ProductInstanceGroup> getGroup(long group_id);

    @Transaction
    @Query("SELECT * FROM productinstancegroup WHERE ( (:productID IS NULL OR product_id = :productID ) AND (:pantryID IS NULL OR pantry_id = :pantryID ) )" )
    public abstract List<ProductInstanceGroupInfo> getListInfoOfAll(@Nullable String productID, @Nullable Long pantryID);
    @Transaction
    @Query("SELECT * FROM productinstancegroup WHERE ( (:productID IS NULL OR product_id = :productID ) AND (:pantryID IS NULL OR pantry_id = :pantryID ) )" )
    public abstract LiveData<List<ProductInstanceGroupInfo>> getLiveInfoOfAll(@Nullable String productID, @Nullable Long pantryID);
    @Transaction
    @Query("SELECT * FROM productinstancegroup AS G INNER JOIN (SELECT pantry_id, owner_id FROM pantries ) AS P ON P.pantry_id = G.pantry_id WHERE ( P.owner_id = :userOwnerID AND (:productID IS NULL OR G.product_id = :productID ) AND (:pantryID IS NULL OR G.pantry_id = :pantryID ) )" )
    public abstract List<ProductInstanceGroupInfo> getInfoOfAll(@NonNull String userOwnerID, @Nullable String productID, @Nullable Long pantryID);


    @Transaction
    @Query("SELECT * FROM productinstancegroup WHERE id IN (:groupID)" )
    public abstract ListenableFuture<List<ProductInstanceGroupInfo>> getInfoOfAll(long... groupID);

    @Query("SELECT * FROM productinstancegroup WHERE product_id = :productID AND pantry_id = :pantryID" )
    public abstract List<ProductInstanceGroup> getAllInstancesOfProduct(String productID, long pantryID);

    final String getProductsWithTags = "SELECT * FROM products AS P INNER JOIN ( SELECT product_id FROM assignedTags AS AT INNER JOIN productTags AS T ON AT.tag_id = T.id WHERE T.name in (:tags) ) as FILTER ON P.id = product_id";
    // String filterByGroupCount = "GROUP BY FILTER.productId HAVING COUNT( FILTER.productId ) >= COUNT( (:tags) ) ORDER BY COUNT( FILTER.productId ) ASC, P.name ASC";

    @Transaction
    @Query( getProductsWithTags /*+ " "+ filterByGroupCount*/)
    /**
     * Get all products with instances that match with at least the specified tags
     * if tags.length > 0 then will be filtered only the products the the specified tags
     * otherwise will include all
     */
    public abstract LiveData<List<ProductWithInstances>> searchAllInstancesOfProduct(String[] tags );


    @Transaction
    @Query( getProductsWithTags + " WHERE P.name LIKE (:content) or P.description LIKE '%' || (:content) || '%' " /*+ filterByGroupCount*/)
    /**
     * Get all products with instances that match with at least the specified tags and content
     */
    public abstract LiveData<List<ProductWithInstances>> searchAllInstancesOfProduct(String[] tags, String content);

    /**
     * Get first entry that match with the same product ID and same pantry ID and others params
     */
    @Query( "SELECT * FROM productinstancegroup WHERE product_id = :productID AND pantry_id = :pantryID AND currentAmountPercent = :currentAmountPercent AND expiryDate = :expiryDate")
    public abstract ProductInstanceGroup getFirstMatchingGroup(String productID, long pantryID, int currentAmountPercent, Date expiryDate );

    @Transaction
    public long mergeInsert(ProductInstanceGroup group) {
        ProductInstanceGroup match = getFirstMatchingGroup(
                group.getProductId(),
                group.getPantryId(),
                group.getCurrentAmountPercent(),
                group.getExpiryDate()
        );

        if( match == null ){
            return insert(group);
        }
        else {
            match.setQuantity(match.getQuantity()+group.getQuantity());
            update(match);
            return match.getId();
        }
    }

    @Transaction
    public int mergeUpdate(ProductInstanceGroup group) {
        ProductInstanceGroup match = getFirstMatchingGroup(
                group.getProductId(),
                group.getPantryId(),
                group.getCurrentAmountPercent(),
                group.getExpiryDate()
        );

        if( match != null ) {
            // merge and update merged
            match.setQuantity(match.getQuantity()+group.getQuantity());
            int count = update(match);
            // delete entry because we just virtually merged it
            if(!Objects.equals(match.getId(), group.getId())){
                delete(group);
            }

            return count;
        }
        else {
            return update(group);
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
