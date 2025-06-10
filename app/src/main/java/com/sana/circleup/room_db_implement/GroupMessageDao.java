package com.sana.circleup.room_db_implement;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction; // Import Transaction annotation

import java.util.List;


@Dao
public interface GroupMessageDao {

    // Insert a single message or replace if a message with the same messageId already exists.
    // Used when syncing messages from Firebase (ChildEventListener onChildAdded/onChildChanged).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateMessage(GroupMessageEntity message);

    // Insert a list of messages or replace existing ones. (Optional, less common for sync)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateMessages(List<GroupMessageEntity> messages);

    // Get all messages for a specific group, ordered by timestamp (ascending). Returns LiveData.
    // This is the primary query used by the Activity to observe messages for the current chat.
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    LiveData<List<GroupMessageEntity>> getMessagesForGroup(String groupId);

    // Get a specific message by its ID. Not LiveData, for specific lookups (e.g., in sync logic).
    @Query("SELECT * FROM group_messages WHERE messageId = :messageId LIMIT 1")
    GroupMessageEntity getMessageById(String messageId);

    // Delete a specific message by its ID (Primary Key).
    // Called when a message is removed from Firebase (ChildEventListener onChildRemoved).
    @Query("DELETE FROM group_messages WHERE messageId = :messageId")
    int deleteMessageById(String messageId); // Returns the number of rows deleted

    // Delete all messages for a specific group.
    // Called when clearing chat or exiting group.
    @Query("DELETE FROM group_messages WHERE groupId = :groupId")
    int deleteAllMessagesForGroup(String groupId);

    // Optional: Delete messages for a group within a Room transaction (good for operations spanning multiple queries).
    @Transaction
    default void clearGroupChatTransaction(String groupId) {
        deleteAllMessagesForGroup(groupId);
    }

    // --- Add this method to delete a message by its FirebaseMessageId ---
//    @Query("DELETE FROM group_messages WHERE firebaseMessageId = :firebaseMessageId")
//    int deleteMessageByFirebaseId(String firebaseMessageId);
//    // --- End of new method --

    @Query("SELECT DISTINCT groupId FROM group_messages ORDER BY timestamp DESC") // Assuming 'group_messages' is your table name and 'timestamp' exists
    LiveData<List<String>> getGroupsWithMessages();
    // --- END NEW ---





//
//    @Query("SELECT * FROM group_messages WHERE groupId = :groupId AND ownerUserId = :ownerUserId ORDER BY timestamp ASC") // Adjust table name 'group_messages'
//    LiveData<List<GroupMessageEntity>> getMessagesForGroup(String groupId, String ownerUserId); // Add ownerUserId filter
//
//    // --- Query for deleting all messages for a group AND owner ---
//    @Query("DELETE FROM group_messages WHERE groupId = :groupId AND ownerUserId = :ownerUserId") // Adjust table name 'group_messages'
//    int deleteAllMessagesForGroupAndOwner(String groupId, String ownerUserId); // Returns number of rows deleted
//    // --- End Query ---
//
//    // Add other delete methods like deleteMessageById if needed
//    @Query("DELETE FROM group_messages WHERE messageId = :messageId AND ownerUserId = :ownerUserId") // Adjust table name 'group_messages'
//    int deleteMessageById(String messageId, String ownerUserId);


}