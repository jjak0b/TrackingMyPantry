package com.jjak0b.android.trackingmypantry.data.services.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstance;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.model.relationships.TagAndProduct;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                Product.class,
                ProductInstance.class,
                ProductTag.class,
                Pantry.class,
                TagAndProduct.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class PantryDB extends RoomDatabase {
    abstract public ProductDao getProductDao();
    abstract public ProductInstanceDao getProductInstanceDao();
    abstract public PantryDao getPantryDao();

    private static volatile PantryDB instance;
    private static final int nTHREADS = 4;
    private static final String DB_NAME = "PantryDB";

    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(nTHREADS);

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {

            });
        }
    };

    public static PantryDB getInstance(final Context context) {
        if ( instance == null) {
            synchronized (PantryDB.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            PantryDB.class,
                            DB_NAME
                    )
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return instance;
    }
}
