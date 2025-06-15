package com.sana.circleup.room_db_implement;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeletedMessageIdDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertDeletedMessageId(DeletedMessageIdEntity deletedMessage);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAllDeletedMessageIds(List<DeletedMessageIdEntity> deletedMessages);

    // This query checks if a message ID exists for the given user
    @Query("SELECT EXISTS(SELECT 1 FROM deleted_messages_for_me WHERE ownerUserId = :ownerUserId AND firebaseMessageId = :firebaseMessageId LIMIT 1)")
    boolean isMessageDeletedForUser(String ownerUserId, String firebaseMessageId);

    // Query to delete a specific message ID for a user from this table
    @Query("DELETE FROM deleted_messages_for_me WHERE ownerUserId = :ownerUserId AND firebaseMessageId = :firebaseMessageId")
    int deleteDeletedMessageId(String ownerUserId, String firebaseMessageId); // Returns number of rows deleted

    // Query to delete all records for a specific user (e.g., if user account is deleted locally)
    @Query("DELETE FROM deleted_messages_for_me WHERE ownerUserId = :ownerUserId")
    int deleteAllDeletedMessagesForUser(String ownerUserId); // Returns number of rows deleted

}