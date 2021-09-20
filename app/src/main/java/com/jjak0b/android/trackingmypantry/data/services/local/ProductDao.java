package com.jjak0b.android.trackingmypantry.data.services.local;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;


import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.model.relationships.TagAndProduct;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class ProductDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insertProductAndAssignedTags(Product p, List<ProductTag> tags ){
        insertProduct(p);
        long[] tag_ids = insertTags(tags);
        int size = tag_ids.length;

        ArrayList<TagAndProduct> assignedTags = new ArrayList<>( size );

        int i = 0;
        for (ProductTag tag : tags) {
            long tagId = tag_ids[ i ] >= 0 ? tag_ids[ i ] : tag.getId();
            assignedTags.add( new TagAndProduct( p.getId(), tagId ) );
            i++;
        }

        insertAssignedTags( assignedTags );
    }

    @Update
    public abstract ListenableFuture<Void> updateProduct(Product p);

    @Update
    public abstract void updateTags(List<ProductTag> tags);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insertProduct(Product p);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long[] insertTags(List<ProductTag> tags);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract ListenableFuture<Void> insertAssignedTags(List<TagAndProduct> assignedTags );

    @Delete
    public abstract ListenableFuture<Void> removeAssignedTags(List<TagAndProduct> assignedTags );

    @Transaction
    @Query( "SELECT * FROM products WHERE id = (:product_id)")
    public abstract LiveData<ProductWithTags> getProductWithTags( String product_id );


    @Query( "SELECT * FROM assignedTags WHERE product_id = (:product_id)")
    public abstract ListenableFuture<List<TagAndProduct>> getProductAssignedTags(String product_id);

    @Transaction
    @Query( "SELECT * FROM products")
    public abstract LiveData<List<ProductWithTags>> getAllProductsWithTags();

    @Query( "SELECT * FROM products")
    public abstract LiveData<List<Product>> getAll();

    @Query( "SELECT * FROM products WHERE barcode = (:barcode)")
    public abstract LiveData<List<Product>> getProductsByBarcode(String barcode);

    @Query( "SELECT * FROM productTags")
    public abstract LiveData<List<ProductTag>> getAllTags();
}
