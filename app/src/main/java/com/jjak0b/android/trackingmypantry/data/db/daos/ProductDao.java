package com.jjak0b.android.trackingmypantry.data.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.db.relationships.TagAndProduct;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class ProductDao {

    @Query("SELECT * FROM assignedTags WHERE product_id = :product_id" )
    public abstract List<TagAndProduct> getAssignedTags( String product_id );

    @Transaction
    public void replaceProductAssignedTags(Product p, List<ProductTag> tags ){
        updateOrInsert(p);
        long[] tag_ids = insertTags(tags);
        int size = tag_ids.length;

        removeAssignedTags(getAssignedTags(p.getBarcode()));
        ArrayList<TagAndProduct> assignedTags = new ArrayList<>( size );
        int i = 0;
        for (ProductTag tag : tags) {
            long tagId = tag_ids[ i ] >= 0 ? tag_ids[ i ] : tag.getId();
            TagAndProduct assigned = new TagAndProduct( p.getBarcode(), tagId );
            assignedTags.add( assigned );
            i++;
        }

        insertAssignedTags( assignedTags );
    }

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void updateProductAndAssignedTags(Product p, List<ProductTag> tags ){
        updateProduct(p);
        long[] tag_ids = insertTags(tags); // insert missing
        int size = tag_ids.length;

        ArrayList<TagAndProduct> assignedTags = new ArrayList<>( size );

        int i = 0;
        for (ProductTag tag : tags) {
            long tagId = tag_ids[ i ] >= 0 ? tag_ids[ i ] : tag.getId();
            assignedTags.add( new TagAndProduct( p.getBarcode(), tagId ) );
            i++;
        }


        insertAssignedTags( assignedTags );
    }

    @Update
    public abstract int updateProduct(Product p);

    @Update
    public abstract void updateTags(List<ProductTag> tags);

    // if using OnConflictStrategy.REPLACE will trigger the OnDelete
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insertProduct(Product p);

    @Transaction
    public int updateOrInsert(Product product) {
        int updatedRows = updateProduct(product);
        if( updatedRows > 0 ) return updatedRows;
        else {
            insertProduct(product);
            return 1;
        }
    }

    @Query("SELECT * FROM products WHERE id = :barcode" )
    public abstract LiveData<Product> get(String barcode);

    @Transaction
    @Query("SELECT * FROM products WHERE id = :barcode" )
    public abstract LiveData<ProductWithTags> getDetails(String barcode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long[] insertTags(List<ProductTag> tags);

    @Delete(entity = Product.class)
    public abstract void remove(Product... products);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertAssignedTags(List<TagAndProduct> assignedTags );
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertAssignedTags(TagAndProduct... assignedTags );

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

    /**
     * get all Product with tags filtered with each following "condition n" in END.
     * condition 1: the product's strings are in OR to match with the respective strings
     * condition 2: the product has at least all the provided tags
     * @param barcode
     * @param name
     * @param description
     * @param tagsIds
     * @return  all Product with tags with a filter applied
     */
    public LiveData<List<ProductWithTags>> getAllProductsWithTags(String barcode, String name, String description, List<Long> tagsIds){
        // call real method, to provide some extra info
        return _getAllProductsWithTags(barcode, name, description, tagsIds, tagsIds.size() );
    }

    @Transaction
    @Query( "SELECT * " +
            "FROM products AS P " +
            "LEFT JOIN assignedTags AS AT ON P.id = AT.product_id AND ( AT.tag_id IN (:tagsIds) )" +
            "WHERE (:barcode IS NULL AND :name IS NULL AND :description IS NULL ) OR (P.id LIKE :barcode OR P.name LIKE :name OR P.description LIKE :description ) " +
            "GROUP BY P.id " +
            "HAVING COUNT(AT.tag_id) >= :tagsCount"

    )
    abstract LiveData<List<ProductWithTags>> _getAllProductsWithTags(String barcode, String name, String description, List<Long> tagsIds, int tagsCount);

    @Query( "SELECT * FROM products")
    public abstract LiveData<List<Product>> getAll();

    @Query( "SELECT * FROM products WHERE id = (:barcode)")
    public abstract LiveData<List<Product>> getProductsByBarcode(String barcode);

    @Query( "SELECT * FROM productTags")
    public abstract LiveData<List<ProductTag>> getAllTags();
}
