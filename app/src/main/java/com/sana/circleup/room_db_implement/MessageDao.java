package com.sana.circleup.room_db_implement; // <-- Replace with your actual package name



import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(MessageEntity message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MessageEntity> messages);

    /**
     * Get all messages for a specific chat thread for the CURRENTLY LOGGED-IN USER (owner).
     * Filters by the ownerUserId AND the participants (from/to).
     * :loggedInUserId is the owner (current user), :partnerId is the other user in the chat.
     * Returns LiveData for observing changes.
     */
    @Query("SELECT * FROM messages_table " +
            "WHERE ownerUserId = :loggedInUserId " + // Filter by the user who owns this copy
            "AND ((`from` = :loggedInUserId AND `to` = :partnerId) OR (`from` = :partnerId AND `to` = :loggedInUserId)) " + // Filter by participants
            "ORDER BY timestamp ASC")
    LiveData<List<MessageEntity>> getMessagesForChat(String loggedInUserId, String partnerId);




    /**
     * Get all messages for a specific chat thread for the CURRENTLY LOGGED-IN USER (owner).
     * Filters by the ownerUserId AND the participants (from/to).
     * :loggedInUserId is the owner (current user), :partnerId is the other user in the chat.
     * Returns a List synchronously (for use on background threads).
     */
    // --- NEW: Synchronous query method ---
    @Query("SELECT * FROM messages_table " +
            "WHERE ownerUserId = :loggedInUserId " + // Filter by the user who owns this copy
            "AND ((`from` = :loggedInUserId AND `to` = :partnerId) OR (`from` = :partnerId AND `to` = :loggedInUserId)) " + // Filter by participants
            "ORDER BY timestamp ASC")
    List<MessageEntity> getMessagesForChatSync(String loggedInUserId, String partnerId);
    // --- END NEW ---


    /**
     * Update the status of a message for a specific user (owner) by its Firebase ID.
     */
    @Query("UPDATE messages_table SET status = :newStatus WHERE firebaseMessageId = :firebaseMsgId AND ownerUserId = :loggedInUserId")
    void updateMessageStatus(String firebaseMsgId, String newStatus, String loggedInUserId);

    /**
     * Update the seen status and seen time of a message for a specific user (owner) by its Firebase ID.
     */
    @Query("UPDATE messages_table SET seen = :seenStatus, seenTime = :seenTime WHERE firebaseMessageId = :firebaseMsgId AND ownerUserId = :loggedInUserId")
    void updateMessageSeen(String firebaseMsgId, boolean seenStatus, String seenTime, String loggedInUserId);

    /**
     * Get a specific message by its Firebase ID and ownerUserId.
     */
    @Query("SELECT * FROM messages_table WHERE firebaseMessageId = :firebaseMsgId AND ownerUserId = :loggedInUserId LIMIT 1")
    MessageEntity getMessageByFirebaseId(String firebaseMsgId, String loggedInUserId); // Pass loggedInUserId

    /**
     * Get all pending messages sent by the current user for THIS chat thread.
     * Filters by ownerUserId (current user), sender (current user), receiver (partner), and status.
     */
    @Query("SELECT * FROM messages_table WHERE ownerUserId = :loggedInUserId AND `from` = :loggedInUserId AND `to` = :partnerId AND status = 'pending'")
    List<MessageEntity> getPendingMessagesForChat(String loggedInUserId, String partnerId);

    /**
     * Delete a message by Firebase ID only for the specific owner.
     */
    @Query("DELETE FROM messages_table WHERE firebaseMessageId = :firebaseMsgId AND ownerUserId = :loggedInUserId")
    int deleteMessageByFirebaseId(String firebaseMsgId, String loggedInUserId); // Pass loggedInUserId


    /**
     * Delete all messages for a specific chat thread for the specific owner.
     * This method takes 3 arguments: the owner's ID, and the two participants' IDs.
     */
    // This seems to be the method you intended to call with 3 arguments
    @Query("DELETE FROM messages_table WHERE ownerUserId = :loggedInUserId AND ((`from` = :user1Id AND `to` = :user2Id) OR (`from` = :user2Id AND `to` = :user1Id))")
    int deleteMessagesForChat(String loggedInUserId, String user1Id, String user2Id); // Keep this method

    /**
     * Delete all messages for a specific chat thread for the specific owner.
     * This method takes 2 arguments: the owner's ID and the chat partner's ID.
     * NOTE: The query logic is identical to the 3-argument version above, just parameter names differ.
     * This might be a duplicate or confusing. You should probably pick one or rename them clearly.
     */
    // This is the method you were trying to call with 3 arguments, causing the error
    @Query("DELETE FROM messages_table WHERE ownerUserId = :ownerUserId AND ((`from` = :ownerUserId AND `to` = :chatPartnerId) OR (`from` = :chatPartnerId AND `to` = :ownerUserId))")
    int deleteAllMessagesForChat(String ownerUserId, String chatPartnerId); // Keep this method if needed, but the call site was wrong

    @Query("DELETE FROM messages_table WHERE ownerUserId = :ownerUserId") // <<< ADD THIS QUERY
    int deleteAllMessagesForOwner(String ownerUserId); // <<< ADD THIS METHOD

    @Query("UPDATE messages_table SET is_revealed = :isRevealed " +
            "WHERE firebaseMessageId = :firebaseMessageId AND ownerUserId = :ownerUserId")
    int updateMessageRevealedStatus(String firebaseMessageId, String ownerUserId, boolean isRevealed);

    // Keep these methods if they are used elsewhere, but they don't seem relevant to the 'Clear Chat' issue for a specific chat
     @Query("DELETE FROM messages_table WHERE ownerUserId = :userId")
     int deleteAllMessagesForChatOwner(String userId); // Remove if not used or clarify purpose




}
