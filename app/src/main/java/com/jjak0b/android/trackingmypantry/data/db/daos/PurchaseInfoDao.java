package com.jjak0b.android.trackingmypantry.data.db.daos;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;

import java.util.List;
import java.util.Map;

@Dao
public interface PurchaseInfoDao {
    @Query("SELECT * FROM purchaseInfo" )
    LiveData<List<PurchaseInfo>> getAll();

    @Query("SELECT * FROM purchaseInfo WHERE id = :purchaseInfo_id" )
    LiveData<PurchaseInfo> getPurchaseInfo(long purchaseInfo_id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertPurchaseInfo(PurchaseInfo purchaseInfo);


    @Query("SELECT I.*, P.*" +
            " FROM (SELECT * FROM purchaseInfo WHERE place_id IS NOT NULL AND product_id = :product_id AND user_id = :user_id) as I " +
            " INNER JOIN places AS P " +
            " ON I.place_id = P.id "
    )
    LiveData<Map<Place, List<PurchaseInfo>>> getAllPurchaseInfo(@NonNull String product_id, @NonNull String user_id);
}

