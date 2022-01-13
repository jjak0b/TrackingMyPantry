package com.jjak0b.android.trackingmypantry.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.jjak0b.android.trackingmypantry.data.db.daos.PantryDao;
import com.jjak0b.android.trackingmypantry.data.db.daos.PlaceDao;
import com.jjak0b.android.trackingmypantry.data.db.daos.ProductDao;
import com.jjak0b.android.trackingmypantry.data.db.daos.ProductInstanceDao;
import com.jjak0b.android.trackingmypantry.data.db.daos.PurchaseInfoDao;
import com.jjak0b.android.trackingmypantry.data.db.entities.Pantry;
import com.jjak0b.android.trackingmypantry.data.db.entities.Place;
import com.jjak0b.android.trackingmypantry.data.db.entities.Product;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.db.entities.ProductTag;
import com.jjak0b.android.trackingmypantry.data.db.entities.PurchaseInfo;
import com.jjak0b.android.trackingmypantry.data.db.entities.User;
import com.jjak0b.android.trackingmypantry.data.db.relationships.TagAndProduct;

import java.util.concurrent.Executors;

@Database(
        entities = {
                User.class,
                Product.class,
                ProductInstanceGroup.class,
                ProductTag.class,
                Pantry.class,
                TagAndProduct.class,
                Place.class,
                PurchaseInfo.class
        },
        version = 1,
        exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class PantryDB extends RoomDatabase {
    abstract public ProductDao getProductDao();
    abstract public ProductInstanceDao getProductInstanceDao();
    abstract public PantryDao getPantryDao();
    abstract public PlaceDao getPlaceDao();
    abstract public PurchaseInfoDao getPurchaseInfoDao();

    private static volatile PantryDB instance;
    private static final int nTHREADS = 4;
    private static final String DB_NAME = "PantryDB";
    // private static final String DB_SCHEMA_PATH = "databases/" + DB_NAME + ".db";

    static final ListeningExecutorService databaseWriteExecutor =
            MoreExecutors.listeningDecorator( Executors.newFixedThreadPool(nTHREADS) );

    public ListeningExecutorService getDBWriteExecutor(){
        return databaseWriteExecutor;
    }

    private static RoomDatabase.Callback initRoomDatabaseCallback(final Context context) {
        return new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);

                databaseWriteExecutor.execute(() -> {
                    try {
                        instance.getPantryDao()
                                .addPantry(
                                        new Pantry(1, context.getResources()
                                                .getString(R.string.pantries_default_pantry_name)
                                        )
                                ).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        };
    }

    public static PantryDB getInstance(final Context context) {
        if ( instance == null) {
            synchronized (PantryDB.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            PantryDB.class,
                            DB_NAME
                    )
                            .addCallback(initRoomDatabaseCallback(context))
                            .build();
                }
            }
        }
        return instance;
    }
}
