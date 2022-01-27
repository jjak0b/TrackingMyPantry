package com.jjak0b.android.trackingmypantry.data.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.jjak0b.android.trackingmypantry.data.db.entities.ProductShared;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.entities.UserProduct;
import com.jjak0b.android.trackingmypantry.data.db.relationships.ProductWithTags;
import com.jjak0b.android.trackingmypantry.data.db.relationships.TagAndUserProductCrossRef;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class ProductDao {

    @Query("SELECT * FROM assignedTags WHERE product_id = :product_id AND owner_id = :ownerID" )
    public abstract List<TagAndUserProductCrossRef> getAssignedTags(String product_id, String ownerID );

    @Transaction
    public void replaceProductAssignedTags(UserProduct p, List<ProductTag> tags ){
        updateOrInsert(p);
        long[] tag_ids = updateOrInsert(tags);
        int size = tag_ids.length;
        String ownerID = p.getUserOwnerId();
        String productID = p.getBarcode();
        List<TagAndUserProductCrossRef> alreadyAssigned = getAssignedTags(productID, ownerID);
        ArrayList<TagAndUserProductCrossRef> toAssign = new ArrayList<>( size );
        int i = 0;
        for (ProductTag tag : tags) {
            long tagId = tag_ids[ i ] > 0 ? tag_ids[ i ] : tag.getId();
            TagAndUserProductCrossRef assigned = new TagAndUserProductCrossRef( productID, ownerID, tagId );
            toAssign.add( assigned );
            i++;
        }

        // tags to add
        toAssign.removeAll(alreadyAssigned);

        // tags to remove
        List<TagAndUserProductCrossRef> toRemove = new ArrayList<>(alreadyAssigned);
        toRemove.removeAll(toAssign);


        removeAssignedTags( toRemove );
        insertAssignedTags( toAssign );
    }

    private long[] updateOrInsert(List<ProductTag> tags) {
        long[] inserted = insertTags(tags);
        ArrayList<ProductTag> alreadyAdded = new ArrayList<>(inserted.length);
        int i = 0;
        for (ProductTag tag : tags) {
            if( inserted[ i ] == 0 ) {
                alreadyAdded.add( tag );
            }
            i++;
        }
        update(alreadyAdded);
        return inserted;
    }

    @Update
    public abstract int update(List<ProductTag> tags);

    @Update
    public abstract int updateProduct(UserProduct p);

    // if using OnConflictStrategy.REPLACE will trigger the OnDelete
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insertProduct(UserProduct p);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insert(ProductShared product);

    @Transaction
    public int updateOrInsert(UserProduct product) {
        insert(new ProductShared(product.getBarcode()));
        int updatedRows = updateProduct(product);
        if( updatedRows > 0 ) return updatedRows;
        else {
            insertProduct(product);
            return 1;
        }
    }

    @Query("SELECT * FROM userProducts WHERE product_id = :barcode AND owner_id = :ownerID" )
    public abstract LiveData<UserProduct> get(String barcode, String ownerID );

    @Transaction
    @Query("SELECT * FROM userProducts WHERE product_id = :barcode AND owner_id = :ownerID" )
    public abstract LiveData<ProductWithTags> getDetails(String barcode, String ownerID);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract long[] insertTags(List<ProductTag> tags);

    @Delete(entity = UserProduct.class)
    public abstract void remove(UserProduct... products);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertAssignedTags(List<TagAndUserProductCrossRef> assignedTags );

    @Delete
    public abstract void removeAssignedTags(List<TagAndUserProductCrossRef> assignedTags );

    @Transaction
    @Query( "SELECT * FROM " +
            " ( SELECT * FROM userProducts WHERE owner_id = :ownerID ) AS P"
           /* " INNER JOIN ( SELECT * FROM assignedTags WHERE owner_id = :ownerID ) AS AT " +
            " INNER JOIN ( SELECT * FROM productTags WHERE owner_id = :ownerID ) AS T" +
            " ON AT.product_id = P.product_id AND AT.tag_id = T.id" +
            " WHERE AT.owner_id = :ownerID AND P.owner_id = :ownerID AND T.owner_id = :ownerID"

            */
    )
    public abstract LiveData<List<ProductWithTags>> getAllProductsWithTags(String ownerID );

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
    public LiveData<List<ProductWithTags>> getAllProductsWithTags(String ownerID, String barcode, String name, String description, List<Long> tagsIds){
        // call real method, to provide some extra info
        return getAllProductsWithTags(ownerID, barcode, name, description, tagsIds, tagsIds.size() );
    }

    @Transaction
    @Query( "SELECT * " +
            "FROM userproducts AS P " +
            "LEFT JOIN assignedTags AS AT ON P.product_id = AT.product_id AND ( AT.tag_id IN (:tagsIds) )" +
            "WHERE P.owner_id = :ownerID" +
            " AND ((:barcode IS NULL AND :name IS NULL AND :description IS NULL ) OR (P.product_id LIKE :barcode OR P.name LIKE :name OR P.description LIKE :description )) " +
            "GROUP BY P.product_id " +
            "HAVING COUNT(AT.tag_id) >= :tagsCount"

    )
    abstract LiveData<List<ProductWithTags>> getAllProductsWithTags(String ownerID, String barcode, String name, String description, List<Long> tagsIds, int tagsCount);

    @Query( "SELECT * FROM userProducts WHERE product_id = (:barcode)")
    public abstract LiveData<List<UserProduct>> getProductsByBarcode(String barcode);

    @Query( "SELECT * FROM productTags WHERE owner_id = :ownerID")
    public abstract LiveData<List<ProductTag>> getAllTags(String ownerID);
}
