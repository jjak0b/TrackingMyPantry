package com.jjak0b.android.trackingmypantry.data.services.local;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.data.model.Place;
import com.jjak0b.android.trackingmypantry.data.model.PurchaseInfo;

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

    @Query("SELECT * FROM purchaseInfo WHERE product_id = :product_id" )
    LiveData<List<PurchaseInfo>> getAllPurchaseInfo(@NonNull String product_id);
}

