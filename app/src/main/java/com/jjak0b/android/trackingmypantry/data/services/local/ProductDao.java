package com.jjak0b.android.trackingmypantry.data.services.local;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;


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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insertProduct(Product p);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract long[] insertTags(List<ProductTag> tags);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insertAssignedTags(List<TagAndProduct> assignedTags );

    @Transaction
    @Query( "SELECT * FROM products WHERE id = (:product_id)")
    public abstract LiveData<ProductWithTags> getProductWithTags( String product_id );

    @Query( "SELECT * FROM products")
    public abstract LiveData<List<Product>> getAll();

    @Query( "SELECT * FROM products WHERE barcode = (:barcode)")
    public abstract LiveData<List<Product>> getProductsByBarcode(String barcode);

    @Query( "SELECT * FROM productTags")
    public abstract LiveData<List<ProductTag>> getAllTags();
}
