package com.sana.circleup.room_db_implement;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserProfileEntity user);

    @Query("SELECT * FROM user_profile WHERE uid = :uid LIMIT 1")
    LiveData<UserProfileEntity> getUser(String uid);

//    @Query("SELECT * FROM user_profile WHERE uid = :uid LIMIT 1")
//    UserProfileEntity getUserProfile(String userId);
}
