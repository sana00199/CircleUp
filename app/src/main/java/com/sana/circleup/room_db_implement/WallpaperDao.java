package com.sana.circleup.room_db_implement;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface WallpaperDao {

    // Insert or replace a wallpaper entry. Use REPLACE strategy.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplaceWallpaper(WallpaperEntity wallpaper);

    // Get wallpaper data for a specific user and conversation
    @Query("SELECT * FROM wallpapers WHERE ownerUserId = :ownerUserId AND conversationId = :conversationId LIMIT 1")
    WallpaperEntity getWallpaper(String ownerUserId, String conversationId);

    // Delete wallpaper for a specific user and conversation
    @Query("DELETE FROM wallpapers WHERE ownerUserId = :ownerUserId AND conversationId = :conversationId")
    int deleteWallpaper(String ownerUserId, String conversationId); // Returns number of rows deleted
}