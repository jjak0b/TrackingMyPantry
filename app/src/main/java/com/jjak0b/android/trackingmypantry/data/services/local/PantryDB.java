package com.jjak0b.android.trackingmypantry.data.services.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.ProductTag;
import com.jjak0b.android.trackingmypantry.data.model.relationships.TagAndProduct;

import java.util.concurrent.Executors;

@Database(
        entities = {
                Product.class,
                ProductInstanceGroup.class,
                ProductTag.class,
                Pantry.class,
                TagAndProduct.class
        },
        version = 1,
        exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class PantryDB extends RoomDatabase {
    abstract public ProductDao getProductDao();
    abstract public ProductInstanceDao getProductInstanceDao();
    abstract public PantryDao getPantryDao();

    private static volatile PantryDB instance;
    private static final int nTHREADS = 4;
    private static final String DB_NAME = "PantryDB";
    private static final String DB_SCHEMA_PATH = "database/" + DB_NAME + ".db";

    static final ListeningExecutorService databaseWriteExecutor =
            MoreExecutors.listeningDecorator( Executors.newFixedThreadPool(nTHREADS) );

    public ListeningExecutorService getDBWriteExecutor(){
        return databaseWriteExecutor;
    }

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
                            // .createFromAsset(DB_SCHEMA_PATH)
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return instance;
    }
}
