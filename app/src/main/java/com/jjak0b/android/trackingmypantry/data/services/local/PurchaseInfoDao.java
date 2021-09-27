package com.jjak0b.android.trackingmypantry.data.services.local;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.model.Place;
import com.jjak0b.android.trackingmypantry.data.model.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.model.relationships.PurchaseInfoWithPlace;

import java.util.List;

@Dao
public interface PurchaseInfoDao {
    @Query("SELECT * FROM purchaseInfo" )
    LiveData<List<PurchaseInfo>> getAll();

    @Query("SELECT * FROM purchaseInfo WHERE id = :purchaseInfo_id" )
    LiveData<PurchaseInfo> getPurchaseInfo(long purchaseInfo_id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    ListenableFuture<Long> insertPurchaseInfo(PurchaseInfo purchaseInfo);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    ListenableFuture<Void> updatePurchaseInfo(PurchaseInfo... purchaseInfo);

    @Transaction
    @Query("SELECT * FROM purchaseInfo WHERE product_id = :product_id" )
    LiveData<List<PurchaseInfoWithPlace>> getAllPurchaseInfo(@NonNull String product_id);
}

