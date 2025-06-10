//package com.sana.circleup.room_db_implement;
//
//import androidx.annotation.NonNull;
//import androidx.room.ColumnInfo;
//import androidx.room.Entity;
//import androidx.room.PrimaryKey;
//
//// Using a composite primary key (roomId, ownerUserId)
//@Entity(tableName = "temporary_rooms",
//        primaryKeys = {"roomId", "owner_user_id"}) // Define composite primary key
//public class TemporaryRoomEntity {
//
//    @NonNull
//    public String roomId; // The ID of the temporary room from Firebase
//
//    // The ID of the user who is logged in and sees this room in their list
//    @ColumnInfo(name = "owner_user_id", index = true) // Index for faster lookups
//    @NonNull
//    public String ownerUserId;
//
//    // Data needed for the list display
//    public String roomName;
//    // Temporary rooms usually don't have images in your structure, but add if needed
//    // public String roomImage;
//    public String lastMessageText; // Latest message content preview
//    public Long lastMessageTimestamp; // Timestamp of the latest message for sorting (use creation time or last message time)
//
//    // Flag to indicate if the current user has unread messages in this room
//    public boolean hasUnreadMessages;
//
//    // Other relevant TemporaryRoom fields from Firebase
//    public String createdBy;
//    public Long expiryTime; // To check if the room has expired locally
//
//    // Room requires a no-argument constructor if you define others
//    public TemporaryRoomEntity() {}
//
//    // Add getters and setters for all fields
//
//    @NonNull
//    public String getRoomId() { return roomId; }
//    public void setRoomId(@NonNull String roomId) { this.roomId = roomId; }
//
//    @NonNull
//    public String getOwnerUserId() { return ownerUserId; }
//    public void setOwnerUserId(@NonNull String ownerUserId) { this.ownerUserId = ownerUserId; }
//
//    public String getRoomName() { return roomName; }
//    public void setRoomName(String roomName) { this.roomName = roomName; }
//
//    // public String getRoomImage() { return roomImage; }
//    // public void setRoomImage(String roomImage) { this.roomImage = roomImage; }
//
//    public String getLastMessageText() { return lastMessageText; }
//    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }
//
//    public Long getLastMessageTimestamp() { return lastMessageTimestamp; }
//    public void setLastMessageTimestamp(Long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }
//
//    public boolean isHasUnreadMessages() { return hasUnreadMessages; }
//    public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }
//
//    public String getCreatedBy() { return createdBy; }
//    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
//
//    public Long getExpiryTime() { return expiryTime; }
//    public void setExpiryTime(Long expiryTime) { this.expiryTime = expiryTime; }
//
//    public Long getTimestamp() {
//
//    }
//}




package com.sana.circleup.room_db_implement;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Using a composite primary key (roomId, ownerUserId)
@Entity(tableName = "temporary_rooms",
        primaryKeys = {"roomId", "owner_user_id"}) // Define composite primary key
public class TemporaryRoomEntity {

    @NonNull
    public String roomId; // The ID of the temporary room from Firebase

    // The ID of the user who is logged in and sees this room in their list
    @ColumnInfo(name = "owner_user_id", index = true) // Index for faster lookups
    @NonNull
    public String ownerUserId;

    // Data needed for the list display
    public String roomName;
    public String lastMessageText; // Latest message content preview
    public Long lastMessageTimestamp; // Timestamp of the latest message for sorting

    // Flag to indicate if the current user has unread messages in this room
    public boolean hasUnreadMessages;

    // Other relevant TemporaryRoom fields from Firebase
    public String createdBy;
    public Long expiryTime; // To check if the room has expired locally

    // *** NEW FIELD for Creation Timestamp ***
    // This corresponds to the 'timestamp' field in your Firebase 'temporaryChatRooms' node
    public Long creationTimestamp;


    // Room requires a no-argument constructor if you define others
    public TemporaryRoomEntity() {}

    // Add getters and setters for all fields (ensure they match field names)

    @NonNull
    public String getRoomId() { return roomId; }
    public void setRoomId(@NonNull String roomId) { this.roomId = roomId; }

    @NonNull
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(@NonNull String ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }

    public Long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(Long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }

    public boolean isHasUnreadMessages() { return hasUnreadMessages; }
    public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Long getExpiryTime() { return expiryTime; }
    public void setExpiryTime(Long expiryTime) { this.expiryTime = expiryTime; }

    // *** FIXED GETTER for Creation Timestamp ***
    // This getter should return the value of the 'creationTimestamp' field
    public Long getTimestamp() {
        return creationTimestamp;
    }
    // *** NEW SETTER for Creation Timestamp ***
    public void setTimestamp(Long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}