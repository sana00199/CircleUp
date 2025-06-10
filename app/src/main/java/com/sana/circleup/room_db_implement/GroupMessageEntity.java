package com.sana.circleup.room_db_implement;

import androidx.room.Entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
// Existing imports...
import androidx.annotation.Nullable; // Import for Nullable annotation if needed

import java.util.Map; // Needed for readBy map


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Map; // Needed for readBy map
import java.util.HashMap; // Needed for default map initialization


import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Import for @Nullable annotation
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

// Define the table name. Add indexes on groupId and timestamp for efficient querying and ordering.
@Entity(tableName = "group_messages", indices = {@Index("groupId"), @Index("timestamp")}) // Add index on timestamp for ordering
public class GroupMessageEntity {

    // The Firebase message push key will be the primary key in Room
    @PrimaryKey
    @NonNull
    private String messageId; // This must be the Firebase push key for the message

    @NonNull // The ID of the group this message belongs to (Used to filter messages by group)
    private String groupId;

    private String messageContent; // The actual message text or Base64 image string
    private String messageType; // "text", "image", "drawing_session" etc.
    private String senderName; // Sender's display name (Denormalized for easier display)
    private String senderId; // Sender's UID (Used to determine if message is sent by current user)

    // Server timestamp - CRUCIAL for sorting messages correctly
    // Firebase ServerValue.TIMESTAMP resolves to a long
    private long timestamp;

    // Client-side date and time (Optional, can be kept for display history if needed)
    // These should ideally be synced from Firebase alongside the timestamp.
    private String date;
    private String time;

    // Store the readBy map. Requires a TypeConverter in ChatDatabase.
    // This Map<String, Boolean> tracks which user UIDs have read the message.
    // Initialize to avoid null map issues.
    // Room automatically handles TypeConverters if defined in the Database class
    private Map<String, Boolean> readBy = new HashMap<>();

    // --- NEW Field for Drawing Session ---
    @Nullable // It can be null for non-drawing messages
    private String drawingSessionId; // Store the ID of the linked drawing session


    // Constructor required by Room (includes all fields marked as columns)
    // Room can use this constructor to build entities from database rows.
    // Make sure ALL database columns are included in ONE constructor Room can use,
    // OR include an empty constructor and setters for all columns.
    // The constructor with all fields is generally safer and preferred by Room.
    // UPDATE this constructor to include the new field
    public GroupMessageEntity(@NonNull String messageId, @NonNull String groupId, String messageContent, String messageType, String senderName, String senderId, long timestamp, String date, String time, Map<String, Boolean> readBy, @Nullable String drawingSessionId) {
        this.messageId = messageId;
        this.groupId = groupId;
        this.messageContent = messageContent;
        this.messageType = messageType;
        this.senderName = senderName;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.date = date;
        this.time = time;
        // Initialize readBy map if the passed map is null for safety, although TypeConverter should handle null
        this.readBy = readBy != null ? readBy : new HashMap<>();
        this.drawingSessionId = drawingSessionId; // Initialize the new field
    }


    // Room often requires an empty constructor if using setters to build the object.
    // If you only use the full constructor above, this might not be strictly needed by Room
    // for entity creation, but it's good practice to keep it if you use setters elsewhere.
    public GroupMessageEntity() {
        this.readBy = new HashMap<>(); // Always initialize map to avoid null issues
        this.drawingSessionId = null; // Default null
    }


    // --- Getters (Needed by Room and for accessing data in the adapter) ---
    @NonNull
    public String getMessageId() { return messageId; }

    @NonNull
    public String getGroupId() { return groupId; }

    public String getMessageContent() { return messageContent; }

    public String getMessageType() { return messageType; }

    public String getSenderName() { return senderName; }

    public String getSenderId() { return senderId; }

    public long getTimestamp() { return timestamp; }

    public String getDate() { return date; }

    public String getTime() { return time; }

    public Map<String, Boolean> getReadBy() { return readBy; } // Getter for readBy map

    // --- NEW Getter for Drawing Session ID ---
    @Nullable
    public String getDrawingSessionId() { return drawingSessionId; }


    // --- Setters (Often needed by Room for population/updates if not using full constructor) ---
    // Room can use setters to populate objects if an empty constructor is present, or for updates.
    public void setMessageId(@NonNull String messageId) { this.messageId = messageId; }
    public void setGroupId(@NonNull String groupId) { this.groupId = groupId; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setReadBy(Map<String, Boolean> readBy) { this.readBy = readBy != null ? readBy : new HashMap<>(); } // Setter for readBy map, handle null

    // --- NEW Setter for Drawing Session ID ---
    public void setDrawingSessionId(@Nullable String drawingSessionId) { this.drawingSessionId = drawingSessionId; }


    // Helper method to check if a user has read this message (Keep this)
    public boolean isReadBy(String userId) {
        return readBy != null && readBy.containsKey(userId) && Boolean.TRUE.equals(readBy.get(userId));
    }

    // Assuming messageId IS the Firebase push key, firebaseMessageId getter/setter are redundant and removed
    // public String getFirebaseMessageId() { return messageId; }
    // public void setFirebaseMessageId(String firebaseMessageId) { this.messageId = firebaseMessageId; }
}