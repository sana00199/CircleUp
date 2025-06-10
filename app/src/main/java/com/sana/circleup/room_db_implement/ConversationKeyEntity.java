package com.sana.circleup.room_db_implement;



import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

// Composite primary key might be better if one user could have multiple keys per conversation
// but given your Firebase structure, one conversation has ONE AES key encrypted for each participant.
// So, ownerUserId + conversationId should be unique.
@Entity(tableName = "conversation_keys",
        primaryKeys = {"owner_user_id", "conversation_id"}) // *** MODIFIED PRIMARY KEY ***
public class ConversationKeyEntity {

    @NonNull
    @ColumnInfo(name = "owner_user_id") // The user whose local DB this entry is in
    private String ownerUserId;

    @NonNull
    @ColumnInfo(name = "conversation_id") // The ID of the chat conversation
    private String conversationId;


    // *** NEW FIELD: Timestamp associated with this key version ***
    // We will use the Firebase ServerValue.TIMESTAMP value from the ConversationKeys entry
    @ColumnInfo(name = "key_timestamp")
    private long keyTimestamp; // Using long to store the timestamp

    @NonNull
    @ColumnInfo(name = "decrypted_key_bytes") // The actual decrypted AES key bytes (Base64 encoded)
    private String decryptedKeyBase64; // Store as Base64 string



    // Add a constructor that includes all fields, including the new timestamp
    public ConversationKeyEntity(@NonNull String ownerUserId, @NonNull String conversationId, @NonNull String decryptedKeyBase64) {
        this.ownerUserId = ownerUserId;
        this.conversationId = conversationId;
        this.keyTimestamp = keyTimestamp; // *** Initialize the new field ***
        this.decryptedKeyBase64 = decryptedKeyBase64;
    }
    // --- Empty constructor for Room ---
    public ConversationKeyEntity() {}


    // --- Getters ---
    @NonNull
    public String getOwnerUserId() {
        return ownerUserId;
    }
    // *** New Getter for keyTimestamp ***
    public long getKeyTimestamp() {
        return keyTimestamp;
    }

    @NonNull
    public String getConversationId() {
        return conversationId;
    }

    @NonNull
    public String getDecryptedKeyBase64() {
        return decryptedKeyBase64;
    }


    // --- Setters (Needed if you use the empty constructor) ---
    public void setOwnerUserId(@NonNull String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public void setConversationId(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    // *** New Setter for keyTimestamp ***
    public void setKeyTimestamp(long keyTimestamp) {
        this.keyTimestamp = keyTimestamp;
    }


    public void setDecryptedKeyBase64(@NonNull String decryptedKeyBase64) {
        this.decryptedKeyBase64 = decryptedKeyBase64;
    }
}