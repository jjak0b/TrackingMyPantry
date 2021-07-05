package com.jjak0b.android.trackingmypantry.data.services.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstance;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithInstances;

import java.util.List;

@Dao
public interface ProductInstanceDao {


    @Transaction
    @Query("SELECT * FROM products" )
    List<ProductWithInstances> getAllInstancesOfProduct();


    String getProductsWithTags = "SELECT * FROM products AS P INNER JOIN ( SELECT product_id FROM assignedTags AS AT INNER JOIN productTags AS T ON AT.tag_id = T.id WHERE T.name in (:tags) ) as FILTER ON P.id = product_id";
    // String filterByGroupCount = "GROUP BY FILTER.productId HAVING COUNT( FILTER.productId ) >= COUNT( (:tags) ) ORDER BY COUNT( FILTER.productId ) ASC, P.name ASC";

    @Transaction
    @Query( getProductsWithTags /*+ " "+ filterByGroupCount*/)
    /**
     * Get all products with instances that match with at least the specified tags
     * if tags.length > 0 then will be filtered only the products the the specified tags
     * otherwise will include all
     */
    LiveData<List<ProductWithInstances>> searchAllInstancesOfProduct(String[] tags );


    @Transaction
    @Query( getProductsWithTags + " WHERE P.name LIKE (:content) or P.description LIKE '%' || (:content) || '%' " /*+ filterByGroupCount*/)
    /**
     * Get all products with instances that match with at least the specified tags and content
     */
    LiveData<List<ProductWithInstances>> searchAllInstancesOfProduct(String[] tags, String content);

    @Insert
    long[] insertAll(ProductInstance... instances);

    @Update
    void updateAll(ProductInstance... instances);

    @Delete
    void deleteAll(ProductInstance... instances);
}
