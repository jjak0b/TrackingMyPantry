package com.jjak0b.android.trackingmypantry.data.db.daos;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.MapInfo;
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
import java.util.Map;

@Dao
public abstract class ProductDao {

    final static String assignedTagsOfOwner = " SELECT AT.*, T.id, T.name " +
            " FROM assignedTags AS AT" +
            " INNER JOIN productTags T " +
            " ON AT.tag_id = T.id AND AT.owner_id = T.owner_id " +
            " WHERE AT.owner_id = :ownerID " +
            " ORDER BY T.name ";

    final static String getProductsWithTagsOfOwner = " SELECT P.*, AT.tag_id, AT.id, AT.name " +
            " FROM userProducts AS P " +
            " LEFT JOIN (" + assignedTagsOfOwner +") AS AT " +
            " ON P.product_id = AT.product_id AND P.owner_id = AT.owner_id " +
            " WHERE P.owner_id = :ownerID" +
            " ORDER BY P.p_name";

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

    @Query("SELECT * FROM ( " + getProductsWithTagsOfOwner +" ) WHERE product_id = :barcode AND owner_id = :ownerID" )
    @MapInfo(keyColumn = "product_id", valueColumn = "tag_id")
    public abstract LiveData<Map<UserProduct, List<ProductTag>>> getMapDetails(String barcode, String ownerID);

    public LiveData<ProductWithTags> getDetails(String barcode, String ownerID) {
        return toProductWithTags(getMapDetails(barcode, ownerID));
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract long[] insertTags(List<ProductTag> tags);

    @Delete(entity = UserProduct.class)
    public abstract void remove(UserProduct... products);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertAssignedTags(List<TagAndUserProductCrossRef> assignedTags );

    @Delete
    public abstract void removeAssignedTags(List<TagAndUserProductCrossRef> assignedTags );

    @Query( "" + getProductsWithTagsOfOwner)
    @MapInfo(keyColumn = "product_id", valueColumn = "id")
    public abstract LiveData<Map<UserProduct, List<ProductTag>>> getMapOfProductWithTags(String ownerID );

    public LiveData<List<ProductWithTags>> getAllProductsWithTags(String ownerID ) {
        return toListOfProductWithTags(getMapOfProductWithTags(ownerID));
        // return toListOfProductWithTags(getMapOfProductWithTags(ownerID, null, null, null, new ArrayList<>(0), 0 ));
    }

    @Query( " SELECT * " +
            " FROM (" + getProductsWithTagsOfOwner +  ") AS P " +
            " INNER JOIN " +
            "   ( SELECT DISTINCT AT.product_id FROM ( " + getProductsWithTagsOfOwner +" ) AS AT " +
                " WHERE (" +
                " ((:barcode IS NULL AND :name IS NULL AND :description IS NULL ) " +
                    " OR (AT.product_id LIKE :barcode OR AT.p_name LIKE :name OR AT.description LIKE :description ))" +
                " ) " +
               " GROUP BY AT.product_id, AT.tag_id" +
                " HAVING :tagsCount = 0 OR ( :tagsCount > 0 AND COUNT( DISTINCT AT.tag_id) >= :tagsCount AND AT.tag_id IN (:tagsIds))" +
            " ) AS AT" +
            " ON P.product_id = AT.product_id" +
            " WHERE P.owner_id = :ownerID " +
            " ORDER BY P.p_name"
    )
    @MapInfo(keyColumn = "product_id", valueColumn = "tag_id")
    public abstract LiveData<Map<UserProduct, List<ProductTag>>> getMapOfProductWithTags(String ownerID, String barcode, String name, String description, List<Long> tagsIds, int tagsCount );

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
        return toListOfProductWithTags(getMapOfProductWithTags(ownerID, barcode, name, description, tagsIds, tagsIds.size() ));
    }

    @Query( "SELECT * FROM userProducts WHERE product_id = (:barcode)")
    public abstract LiveData<List<UserProduct>> getProductsByBarcode(String barcode);

    @Query( "SELECT * FROM productTags WHERE owner_id = :ownerID")
    public abstract LiveData<List<ProductTag>> getAllTags(String ownerID);


    static LiveData<ProductWithTags> toProductWithTags(LiveData<Map<UserProduct, List<ProductTag>>> liveData ) {
        return Transformations.map(toListOfProductWithTags(liveData), input -> {
           return input != null && input.size() > 0 ? input.get(0): null;
        });
    }

    static LiveData<List<ProductWithTags>> toListOfProductWithTags(LiveData<Map<UserProduct, List<ProductTag>>> liveData ) {
        return Transformations.map(liveData, input -> {
            List<ProductWithTags> list = new ArrayList<>(input.keySet().size());
            for ( Map.Entry<UserProduct, List<ProductTag>> entry : input.entrySet() ) {
                ProductWithTags p = new ProductWithTags();
                p.product = entry.getKey();
                p.tags = entry.getValue();
                // Note: this check is required because Room's Map doesn't remove "null" values that exists in query result rows
                // This case happen whens product doesn't have any tag assigned by the user
                // So Room map the product to a list with a single ProductTag, with all values to null/0
                if( p.tags.size() == 1 && ProductTag.isDummy(p.tags.get(0)) )
                    p.tags.remove(0);

                Log.e("Dao", "" + p.tags );
                list.add(p);
            }

            return list;
        });
    }
}
