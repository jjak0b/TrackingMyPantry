package com.jjak0b.android.trackingmypantry.data.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.jjak0b.android.trackingmypantry.data.db.entities.User;

import java.util.List;

@Dao
public abstract class UserDao {
    @Query("SELECT * FROM users" )
    public abstract LiveData<List<User>> getAll();

    @Query("SELECT * FROM users WHERE id = :user_id" )
    public abstract LiveData<User> get(String user_id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insert(User user);

    @Update
    abstract int update(User user);

    @Delete
    public abstract void remove(User user);

    @Transaction
    public int updateOrInsert(User user) {
        int rowsUpdated = update(user);
        if( rowsUpdated > 0 ){
            return rowsUpdated;
        }
        else {
            insert(user);
            return 1;
        }
    }
}
