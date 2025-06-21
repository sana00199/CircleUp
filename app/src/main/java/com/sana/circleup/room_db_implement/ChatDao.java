package com.sana.circleup.room_db_implement; // Make sure your package is correct

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;



@Dao // Annotation to make this an official Room DAO
public interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateChat(ChatEntity chat);

    // Query to get the sum of unread messages for chats owned by a user
    @Query("SELECT SUM(unreadCount) FROM chat_list_table WHERE ownerUserId = :ownerUserId")
    int getTotalUnreadMessageCount(String ownerUserId); // Returns 0 if no rows or sum is null

    /**
     * Gets all chat list entries from the 'chat_list_table' that are owned by a specific logged-in user.
     * The results are ordered by timestamp (latest message timestamp) in descending order (latest first).
     * This is used to populate the main chat list UI.
     * Filters by the 'ownerUserId' column.
     */
    @Query("SELECT * FROM chat_list_table WHERE ownerUserId = :loggedInUserId ORDER BY timestamp DESC") // Corrected column name
    LiveData<List<ChatEntity>> getAllChats(String loggedInUserId);


    // Inside ChatDao.java

// ... (existing DAO methods) ...

    /**
     * Updates the partnerKeysChanged flag for a specific chat entry owned by a user.
     */
    @Query("UPDATE chat_list_table SET partnerKeysChanged = :flagValue WHERE ownerUserId = :ownerId AND userId = :partnerId") // Assuming chats_table is your table name
    int updatePartnerKeysChangedFlag(String ownerId, String partnerId, boolean flagValue); // <-- NEW DAO method

    /**
     * Gets a specific chat list entry from the 'chat_list_table' for a particular chat partner (otherUser),
     * filtering by the 'userId' (the chat partner's ID) AND the 'ownerUserId' (the logged-in user's ID).
     * LIMIT 1 is used as there should only be one such entry for this combination.
     * This might be useful if you need details about a specific chat entry (like its unread count) elsewhere.
     */
    @Query("SELECT * FROM chat_list_table WHERE userId = :otherUserId AND ownerUserId = :loggedInUserId LIMIT 1") // Corrected column name
    ChatEntity getChatByUserId(String otherUserId, String loggedInUserId);

    /**
     * Deletes a specific chat list entry from the 'chat_list_table'.
     * Filters by the 'userId' (the chat partner's ID) AND the 'ownerUserId' (the logged-in user's ID)
     * to ensure we only delete the entry owned by the current user from their list.
     *
     * @return The number of rows deleted.
     */
    @Query("DELETE FROM chat_list_table WHERE userId = :otherUserId AND ownerUserId = :loggedInUserId") // Corrected column name
    int deleteChatByUserId(String otherUserId, String loggedInUserId);

    /**
     * Updates the 'seen' status of a chat entry in the 'chat_list_table'.
     * Filters by 'userId' and 'ownerUserId'.
     * Note: 'seen' status in ChatEntity usually reflects whether the *last message* in that chat
     * has been seen by the owner user.
     */
//    @Query("UPDATE chat_list_table SET seen = :seenStatus WHERE userId = :otherUserId AND ownerUserId = :loggedInUserId") // Corrected column names
//    void updateSeenStatus(String otherUserId, boolean seenStatus, String loggedInUserId);

    /**
     * Updates the 'unreadCount' of a chat entry in the 'chat_list_table'.
     * Filters by 'userId' and 'ownerUserId'.
     *
     * @return
     */
    @Query("UPDATE chat_list_table SET unreadCount = :count WHERE userId = :otherUserId AND ownerUserId = :loggedInUserId") // Corrected column names
    int updateUnreadCount(String otherUserId, int count, String loggedInUserId);

    /**
     * Checks if a chat entry exists in the 'chat_list_table' for a specific chat partner,
     * filtering by 'userId' and 'ownerUserId'. Returns true if at least one such entry exists.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM chat_list_table WHERE userId = :otherUserId AND ownerUserId = :loggedInUserId LIMIT 1)") // Corrected column name
    boolean chatExists(String otherUserId, String loggedInUserId);

    /**
     * Deletes all chat list entries from the 'chat_list_table' that are owned by a specific logged-in user.
     * This is typically used during user logout or account deletion to clear local chat list data.
     *
     * @return
     */
    @Query("DELETE FROM chat_list_table WHERE ownerUserId = :loggedInUserId") // Corrected column name
    int deleteAllChatsForOwner(String loggedInUserId);


    // --- Methods for managing individual Messages (MessageEntity) ---
    // These are needed by ChatPageActivity and potentially ChatFragment's sync logic for unread counts (if not calculated directly from summary)

    /**
     * Inserts a new message into the 'messages_table' or updates an existing one.
     * Uses OnConflictStrategy.REPLACE, meaning if an entry with the same primary key (firebaseMessageId)
     * and owner (ownerUserId) already exists, it will be replaced with the new data.
     * Make sure MessageEntity has a composite primary key or unique index on (firebaseMessageId, ownerUserId)
     * for REPLACE strategy to work correctly for per-user messages.
     * Make sure MessageEntity has firebaseMessageId, from, to, type, message, ownerUserId, etc. set.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(MessageEntity message);

    /**
     * Gets all chat list entries immediately (synchronously) from the 'chat_list_table'
     * that are owned by a specific logged-in user.
     * Used for sync logic where a LiveData is not suitable.
     */
    @Query("SELECT * FROM chat_list_table WHERE ownerUserId = :loggedInUserId") // Corrected column name
    List<ChatEntity> getAllChatsImmediate(String loggedInUserId);


    // Add the MessageDao queries needed by ChatPageActivity here or in a separate MessageDao interface
    // Based on the previous code, you will need these MessageDao queries (assuming they are in THIS ChatDao interface):

    /**
     * Gets all messages for a specific chat between two users, ordered by timestamp.
     * Messages must be owned by the specified ownerId.
     * The query needs to select messages where (from=user1 AND to=user2) OR (from=user2 AND to=user1).
     *
     * @param ownerId The ID of the user whose copy of the messages is being retrieved.
     * @param user1Id One participant ID.
     * @param user2Id The other participant ID.
     * @return LiveData list of messages for the chat.
     */
    @Query("SELECT * FROM messages_table WHERE ownerUserId = :ownerId AND ((`from` = :user1Id AND `to` = :user2Id) OR (`from` = :user2Id AND `to` = :user1Id)) ORDER BY timestamp ASC")
    LiveData<List<MessageEntity>> getMessagesForChat(String ownerId, String user1Id, String user2Id);


    /**
     * Updates the status of a specific message owned by a user.
     *
     * @param firebaseMessageId The Firebase ID of the message.
     * @param status            The new status ("sent", "failed", etc.).
     * @param ownerId           The ID of the user whose copy is being updated.
     * @return The number of rows updated.
     */
    @Query("UPDATE messages_table SET status = :status WHERE firebaseMessageId = :firebaseMessageId AND ownerUserId = :ownerId")
    int updateMessageStatus(String firebaseMessageId, String status, String ownerId);


    /**
     * Gets a specific message by its Firebase ID and owner ID.
     *
     * @param firebaseMessageId The Firebase ID of the message.
     * @param ownerId           The ID of the user who owns this copy.
     * @return The MessageEntity, or null if not found.
     */
    @Query("SELECT * FROM messages_table WHERE firebaseMessageId = :firebaseMessageId AND ownerUserId = :ownerId LIMIT 1")
    MessageEntity getMessageByFirebaseId(String firebaseMessageId, String ownerId);


    /**
     * Deletes a specific message owned by a user.
     *
     * @param firebaseMessageId The Firebase ID of the message.
     * @param ownerId           The ID of the user who owns this copy.
     * @return The number of rows deleted.
     */
    @Query("DELETE FROM messages_table WHERE firebaseMessageId = :firebaseMessageId AND ownerUserId = :ownerId")
    int deleteMessageByFirebaseId(String firebaseMessageId, String ownerId);


    /**
     * Gets all pending messages sent by a specific user, owned by that same user.
     * Used for retrying failed messages.
     *
     * @param ownerId  The ID of the user who owns this copy (current user).
     * @param senderId The ID of the sender of the message (should be the same as ownerId for pending outgoing).
     * @return List of pending MessageEntity objects.
     */
    @Query("SELECT * FROM messages_table WHERE ownerUserId = :ownerId AND `from` = :senderId AND status = 'pending' ORDER BY timestamp ASC")
    List<MessageEntity> getPendingMessagesForChat(String ownerId, String senderId);

    /**
     * Deletes all messages for a specific chat between two users, owned by a specific user.
     * This is used during chat deletion.
     *
     * @param ownerId The ID of the user whose messages are being deleted.
     * @param user1Id One participant ID.
     * @param user2Id The other participant ID.
     * @return The number of rows deleted.
     */
    @Query("DELETE FROM messages_table WHERE ownerUserId = :ownerId AND ((`from` = :user1Id AND `to` = :user2Id) OR (`from` = :user2Id AND `to` = :user1Id))")
    int deleteMessagesForChat(String ownerId, String user1Id, String user2Id);

    /**
     * Deletes all messages for a specific chat between two users, regardless of who owns them locally.
     * USE WITH CAUTION. Usually, you only delete messages *owned* by the current user.
     * This is less likely needed unless implementing multi-device sync cleanup differently.
     *
     * @param user1Id One participant ID.
     * @param user2Id The other participant ID.
     * @return The number of rows deleted.
     */
//     @Query("DELETE FROM messages_table WHERE (`from` = :user1Id AND `to` = :user2Id) OR (`from` = :user2Id AND `to` = :user1Id)")
//     int deleteAllMessagesForChatGlobally(String user1Id, String user2Id);

    /**
     * Deletes all messages owned by a specific user.
     * Used during logout or account deletion for messages.
     *
     * @param ownerId The ID of the user who owns these messages.
     * @return The number of rows deleted.
     */
    @Query("DELETE FROM messages_table WHERE ownerUserId = :ownerId")
    int deleteAllMessagesForOwner(String ownerId);
}




