package com.sana.circleup.room_db_implement;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

// Annotate as a Room Data Access Object (DAO)


// Annotate as a Room Data Access Object (DAO)
@Dao
public interface GroupListDao {

    // --- Operations for GroupEntity ---

    /**
     * Inserts a new GroupEntity or updates an existing one.
     * OnConflictStrategy.REPLACE will replace the old entry if the composite primary key (groupId, ownerUserId) exists.
     * Make sure the ownerUserId is set on the GroupEntity object before calling this.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateGroup(GroupEntity group);

    /**
     * Gets all GroupEntity entries for a specific logged-in user, ordered by the latest message timestamp (latest first).
     * This query filters by the owner_user_id column.
     */
    @Query("SELECT * FROM groupss WHERE owner_user_id = :loggedInUserId ORDER BY lastMessageTimestamp DESC")
    LiveData<List<GroupEntity>> getAllGroupsForOwner(String loggedInUserId);

    /**
     * Gets a specific GroupEntity by its ID and the owner's ID.
     * Used for retrieving a single group entry owned by the current user.
     */
    @Query("SELECT * FROM groupss WHERE groupId = :groupId AND owner_user_id = :loggedInUserId LIMIT 1")
    GroupEntity getGroupById(String groupId, String loggedInUserId);


    /**
     * Deletes a specific GroupEntity for an owner (using composite key fields).
     */
    @Query("DELETE FROM groupss WHERE groupId = :groupId AND owner_user_id = :loggedInUserId")
    void deleteGroupForOwner(String groupId, String loggedInUserId);


    // --- Operations for TemporaryRoomEntity ---

    /**
     * Inserts a new TemporaryRoomEntity or updates an existing one.
     * OnConflictStrategy.REPLACE will replace the old entry if the composite primary key (roomId, ownerUserId) exists.
     * Make sure the ownerUserId is set on the TemporaryRoomEntity object before calling this.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateTemporaryRoom(TemporaryRoomEntity room);

    /**
     * Gets all TemporaryRoomEntity entries for a specific logged-in user, ordered by the latest message timestamp (latest first).
     * This query filters by the owner_user_id column.
     */
    @Query("SELECT * FROM temporary_rooms WHERE owner_user_id = :loggedInUserId ORDER BY lastMessageTimestamp DESC")
    LiveData<List<TemporaryRoomEntity>> getAllTemporaryRoomsForOwner(String loggedInUserId);

    /**
     * Gets a specific TemporaryRoomEntity by its ID and the owner's ID.
     * Used for retrieving a single temporary room entry owned by the current user.
     */
    @Query("SELECT * FROM temporary_rooms WHERE roomId = :roomId AND owner_user_id = :loggedInUserId LIMIT 1")
    TemporaryRoomEntity getTemporaryRoomById(String roomId, String loggedInUserId);


    /**
     * Deletes a specific TemporaryRoomEntity for an owner (using composite key fields).
     */
    @Query("DELETE FROM temporary_rooms WHERE roomId = :roomId AND owner_user_id = :loggedInUserId")
    void deleteTemporaryRoomForOwner(String roomId, String loggedInUserId);


    // --- Cleanup Operations (for logout) ---

    /**
     * Deletes all Group entities owned by a specific logged-in user.
     * This is part of the cleanup process when a user logs out.
     *
     * @return
     */
    @Query("DELETE FROM groupss WHERE owner_user_id = :loggedInUserId")
    int deleteAllGroupsForOwner(String loggedInUserId);

    /**
     * Deletes all Temporary Room entities owned by a specific logged-in user.
     * This is part of the cleanup process when a user logs out.
     *
     * @return
     */
    @Query("DELETE FROM temporary_rooms WHERE owner_user_id = :loggedInUserId")
    int deleteAllTemporaryRoomsForOwner(String loggedInUserId);

    // Note: Room supports transactions if you need to perform multiple deletions atomically,
    // but for simple cleanup like this, separate calls are often sufficient.

    @Query("DELETE FROM groupss WHERE owner_user_id = :ownerUserId AND groupId NOT IN (:presentGroupIds)")
    void deleteGroupsNotPresent(String ownerUserId, List<String> presentGroupIds);

    @Query("DELETE FROM temporary_rooms WHERE owner_user_id = :ownerUserId AND roomId NOT IN (:presentRoomIds)")
    void deleteTemporaryRoomsNotPresent(String ownerUserId, List<String> presentRoomIds);



    // --- NEW: Queries for Group Cleanup ---
    @Query("SELECT groupId FROM groupss WHERE owner_user_id = :loggedInUserId")
    List<String> getGroupIdsForOwner(String loggedInUserId);

    @Query("DELETE FROM groupss WHERE owner_user_id = :ownerUserId AND groupId IN (:groupIds)")
    void deleteGroupsByIdsForOwner(String ownerUserId, List<String> groupIds);


    // ... (Existing DAO methods: insertOrUpdateTemporaryRoom, getAllTemporaryRoomsForOwner, getTemporaryRoomById, deleteTemporaryRoomForOwner) ...

    // --- NEW: Queries for Temporary Room Cleanup ---
    @Query("SELECT roomId FROM temporary_rooms WHERE owner_user_id = :loggedInUserId")
    List<String> getTemporaryRoomIdsForOwner(String loggedInUserId);



    @Query("DELETE FROM temporary_rooms WHERE owner_user_id = :ownerUserId AND roomId IN (:roomIds)")
    void deleteTemporaryRoomsByIdsForOwner(String ownerUserId, List<String> roomIds);


}