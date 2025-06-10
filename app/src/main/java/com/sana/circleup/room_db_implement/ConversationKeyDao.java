package com.sana.circleup.room_db_implement;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sana.circleup.room_db_implement.ConversationKeyEntity; // Adjust package

import java.util.List;





import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.annotation.Nullable; // Import for nullable return types

import com.sana.circleup.room_db_implement.ConversationKeyEntity; // Adjust package

import java.util.List;

@Dao
public interface ConversationKeyDao {

    /**
     * Inserts or updates the single decrypted conversation key for this owner/conversation pair.
     * Uses REPLACE strategy, so if a key for the same owner and conversation already exists, it's replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateKey(ConversationKeyEntity key);

    // REMOVED: getKeyByTimestamp - Not needed in single key model

    /**
     * Retrieves the single decrypted conversation key for a user and conversation.
     * This is the main method to get the key for a specific chat.
     * Returns null if not found.
     */
    @Nullable // Indicate that null is a possible return value
    @Query("SELECT * FROM conversation_keys WHERE owner_user_id = :ownerUserId AND conversation_id = :conversationId LIMIT 1") // LIMIT 1 is redundant with PK but harmless
    ConversationKeyEntity getKeyForConversation(String ownerUserId, String conversationId); // Renamed from getKeyById for clarity

    // REMOVED: getAllKeysForConversation (returning List) - Not needed in single key model, getKeyForConversation is sufficient

    /**
     * Retrieves ALL decrypted conversation keys for a specific user.
     * Useful for loading the cache on login/unlock.
     * Ordered by conversation ID for consistency (optional).
     * Returns an empty list if none found.
     */
    @Query("SELECT * FROM conversation_keys WHERE owner_user_id = :ownerUserId ORDER BY conversation_id ASC")
    List<ConversationKeyEntity> getAllKeys(String ownerUserId);

    /**
     * Deletes the single conversation key for a user and conversation.
     *
     * @return The number of rows deleted (should be 0 or 1).
     */
    @Query("DELETE FROM conversation_keys WHERE owner_user_id = :ownerUserId AND conversation_id = :conversationId")
    int deleteKeyForConversation(String ownerUserId, String conversationId); // Renamed from deleteKeyById for clarity

    // REMOVED: deleteSpecificKeyVersion - Not needed in single key model

    /**
     * Deletes the single conversation key for a user and conversation.
     * This method is often used when deleting a chat. It's the same as deleteKeyForConversation.
     *
     * @return The number of rows deleted (should be 0 or 1).
     */
    @Query("DELETE FROM conversation_keys WHERE owner_user_id = :ownerUserId AND conversation_id = :conversationId")
    int deleteAllKeysForConversation(String ownerUserId, String conversationId); // This method name is slightly misleading for "single key", but matches the previous usage for deleting by conv ID. It correctly deletes the single row.

    /**
     * Deletes ALL conversation keys for a specific user.
     * Called during key reset or logout.
     *
     * @return The number of rows deleted.
     */
    @Query("DELETE FROM conversation_keys WHERE owner_user_id = :ownerUserId")
    int deleteAllKeysForOwner(String ownerUserId);
}



//@Dao
//public interface ConversationKeyDao {
//
//    /**
//     * Inserts or updates a decrypted conversation key in the database.
//     * Uses REPLACE strategy, so if a key for the same owner and conversation already exists, it's replaced.
//     */
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insertOrUpdateKey(ConversationKeyEntity key);
//
//    @Query("SELECT * FROM conversation_keys WHERE owner_user_id = :ownerUserId AND conversation_id = :conversationId AND key_timestamp = :keyTimestamp LIMIT 1")
//    ConversationKeyEntity getKeyByTimestamp(String ownerUserId, String conversationId, long keyTimestamp); // Returns null if not found
//
//    /**
//     * Retrieves a specific decrypted conversation key for a user and conversation.
//     */
//    @Query("SELECT * FROM conversation_keys WHERE owner_user_id = :ownerUserId AND conversation_id = :conversationId LIMIT 1")
//    ConversationKeyEntity getKeyById(String ownerUserId, String conversationId); // Returns null if not found
//
//
//
//    @Query("SELECT * FROM conversation_keys WHERE owner_user_id = :ownerUserId AND conversation_id = :conversationId ORDER BY key_timestamp ASC")
//    List<ConversationKeyEntity> getAllKeysForConversation(String ownerUserId, String conversationId); // Returns empty list if none found
//
//
//    /**
//     * Retrieves ALL decrypted conversation keys for a specific user.
//     * Ordered by conversation ID for consistency (optional).
//     */
//    @Query("SELECT * FROM conversation_keys WHERE owner_user_id = :ownerUserId ORDER BY conversation_id ASC")
//    List<ConversationKeyEntity> getAllKeys(String ownerUserId); // Returns empty list if none found
//
//    /**
//     * Deletes a specific conversation key for a user and conversation.
//     *
//     * @return The number of rows deleted.
//     */
//    @Query("DELETE FROM conversation_keys WHERE owner_user_id = :ownerUserId AND conversation_id = :conversationId")
//    int deleteKeyById(String ownerUserId, String conversationId);
//
//    @Query("DELETE FROM conversation_keys WHERE owner_user_id = :ownerUserId AND conversation_id = :conversationId AND key_timestamp = :keyTimestamp")
//    int deleteSpecificKeyVersion(String ownerUserId, String conversationId, long keyTimestamp);
//
//
//    @Query("DELETE FROM conversation_keys WHERE owner_user_id = :ownerUserId AND conversation_id = :conversationId")
//    int deleteAllKeysForConversation(String ownerUserId, String conversationId); // Delete all versions matching owner and conv ID
//
//    @Query("DELETE FROM conversation_keys WHERE owner_user_id = :ownerUserId")
//    int deleteAllKeysForOwner(String ownerUserId); // Keep this method, it will delete all rows for the owner regardless of timestamp
//
//
//    int deleteKeyForConversation(String currentUserID, String conversationIdForKeyDelete);
//
//    ConversationKeyEntity getKeyForConversation(String currentUserID, String conversationId);
//}
