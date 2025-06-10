package com.sana.circleup.room_db_implement;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import java.util.List;

@Dao
public interface UserDao {

    // Insert a single user or replace if userId already exists.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateUser(UserEntity user);

    // Insert a list of users or replace existing ones.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllUsers(List<UserEntity> users);

    // Get all users from the database, ordered by username. Returns LiveData.
    @Query("SELECT * FROM users ORDER BY username ASC")
    LiveData<List<UserEntity>> getAllUsers();

    // Get a specific user by UID. Not LiveData, for specific lookups (e.g., in sync logic).
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    UserEntity getUserById(String userId);

    // Search users by username (case-insensitive search using LIKE). Returns LiveData.
    // '%': wildcard character for any sequence of characters.
    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' ORDER BY username ASC")
    LiveData<List<UserEntity>> searchUsersByUsername(String query);

    // Optional: Get users whose username starts with a specific string (for prefix search).
    @Query("SELECT * FROM users WHERE username LIKE :prefix || '%' ORDER BY username ASC")
    LiveData<List<UserEntity>> getUsersStartingWith(String prefix);


    // Delete a specific user by UID.
    @Query("DELETE FROM users WHERE userId = :userId")
    int deleteUserById(String userId); // Returns the number of rows deleted (optional return type)

    // Delete all users from the table (e.g., on logout or complete resync).
    @Query("DELETE FROM users")
    void deleteAllUsers();
}
