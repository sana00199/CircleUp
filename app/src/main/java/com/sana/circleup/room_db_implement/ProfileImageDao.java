package com.sana.circleup.room_db_implement;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;

@Dao
public interface ProfileImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProfileImage(ProfileImageEntity profileImage);

    @Query("SELECT profileImage FROM profile_images WHERE userId = :userId")
    LiveData<String> getProfileImage(String userId);

    @Query("DELETE FROM profile_images WHERE userId = :userId")
    void deleteProfileImage(String userId);
}
