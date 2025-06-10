package com.sana.circleup.temporary_chat_room;

import com.sana.circleup.room_db_implement.TemporaryRoomEntity;

//public class TemporaryRoom implements TemporaryIChatListItemGroup { // Implement the interface
//    private String roomId;
//    private String roomName;
//    private String createdBy;
//    private Long expiryTime; // Timestamp in milliseconds
//    private Long timestamp; // Creation timestamp from ServerValue.TIMESTAMP
//    private boolean hasUnreadMessages = false;
//
//    // You might need fields for latest message and its timestamp for sorting/preview
//    private String lastMessageText;
//    private long lastMessageTimestamp;
//    private Long creationTimestamp; // Keep creation timestamp if useful
//
//
//    public TemporaryRoom() {
//        // Default constructor needed for Firebase
//    }
//
//    public TemporaryRoom(String roomId, String roomName, String createdBy, Long expiryTime, Long timestamp, String lastMessageText, long lastMessageTimestamp) {
//        this.roomId = roomId;
//        this.roomName = roomName;
//        this.createdBy = createdBy;
//        this.expiryTime = expiryTime;
//        this.timestamp = timestamp; // Use the timestamp from Firebase
//        this.lastMessageText = lastMessageText;
//        this.lastMessageTimestamp = lastMessageTimestamp;
//    }
//
//
//
//    // In your TemporaryRoom.java model class
//// *** NEW Constructor to create model from Room Entity ***
//    public TemporaryRoom(TemporaryRoomEntity entity) {
//        this.roomId = entity.getRoomId();
//        this.roomName = entity.getRoomName();
//        this.createdBy = entity.getCreatedBy();
//        this.expiryTime = entity.getExpiryTime();
//        // This model might not strictly need creationTimestamp if using lastMessageTimestamp for sorting
//        // this.creationTimestamp = entity.getCreationTimestamp(); // Add if you put this in Entity
//        this.lastMessageText = entity.getLastMessageText();
//        this.lastMessageTimestamp = entity.getLastMessageTimestamp();
//        this.hasUnreadMessages = entity.isHasUnreadMessages();
//    }
//
//
//    // --- Implement IChatListItem methods ---
//    @Override
//    public String getId() {
//        return roomId;
//    }
//
//    @Override
//    public String getName() {
//        return roomName;
//    }
//
//    @Override
//    public String getImageUrl() {
//        // Temporary rooms might not have images, return null or a default temp room image
//        return null;
//    }
//
//    @Override
//    public boolean hasUnreadMessages() {
//        return hasUnreadMessages;
//    }
//
//    @Override
//    public long getSortingTimestamp() {
//        // For temporary rooms, you might sort by the timestamp of the latest message,
//        // similar to groups. Or maybe by creation timestamp, or even remaining expiry time.
//        // Let's sort by latest message timestamp for consistency with groups.
//        return lastMessageTimestamp > 0 ? lastMessageTimestamp : (timestamp != null ? timestamp : 0);
//    }
//
//    @Override
////    public String getLastMessagePreview() {
////        return "";
////    }
//
//    public String getLastMessagePreview() {
//        // Return the last message text for preview
//        return lastMessageText != null ? lastMessageText : "";
//    }
//
//    // Add getter for last message text if needed for preview
//    public String getLastMessageText() {
//        return lastMessageText != null ? lastMessageText : ""; }
//
//
//    // --- Getters and Setters for TemporaryRoom specific fields ---
//    public String getRoomId() { return roomId; } // Duplicate, but can keep
//    public String getCreatedBy() { return createdBy; }
//    public Long getExpiryTime() { return expiryTime; }
//    public Long getTimestamp() { return timestamp; } // Creation timestamp
//
//    public void setRoomId(String roomId) { this.roomId = roomId; }
//    public void setRoomName(String roomName) { this.roomName = roomName; }
//    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
//    public void setExpiryTime(Long expiryTime) { this.expiryTime = expiryTime; }
//    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
//    public void setUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }
//
//    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }
//    public void setLastMessageTimestamp(long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }
//
//
//    // Method to check if expired (client-side check)
//    public boolean isExpired() {
//        return expiryTime != null && System.currentTimeMillis() > expiryTime;
//    }
//}




import java.util.Map;

public class TemporaryRoom implements TemporaryIChatListItemGroup { // Implement the interface
    private String roomId;
    private String roomName;
    private String createdBy;
    private Long expiryTime; // Timestamp in milliseconds
    private Long timestamp; // Creation timestamp from ServerValue.TIMESTAMP (This is the 'timestamp' field in Firebase)
    private boolean hasUnreadMessages = false;

    // Fields for latest message and its timestamp for sorting/preview
    private String lastMessageText;
    private Long lastMessageTimestamp; // <-- Changed from 'long' to 'Long'
    // Removed creationTimestamp field as 'timestamp' serves this purpose

    // For read status if you store it per message in Firebase and need it in the model (likely managed in GroupMessage)
    // private Map<String, Object> readBy; // Already in GroupMessage, maybe not needed here


    public TemporaryRoom() {
        // Default constructor needed for Firebase
    }

    // Constructor from Firebase data (for list items, Room Entity constructor is primary)
    // public TemporaryRoom(String roomId, String roomName, String createdBy, Long expiryTime, Long timestamp, String lastMessageText, Long lastMessageTimestamp) {
    //     this.roomId = roomId;
    //     this.roomName = roomName;
    //     this.createdBy = createdBy;
    //     this.expiryTime = expiryTime;
    //     this.timestamp = timestamp; // Creation timestamp
    //     this.lastMessageText = lastMessageText;
    //     this.lastMessageTimestamp = lastMessageTimestamp; // Accept Long here
    // }


    // *** NEW Constructor to create model from Room Entity ***
    public TemporaryRoom(TemporaryRoomEntity entity) {
        this.roomId = entity.getRoomId();
        this.roomName = entity.getRoomName();
        this.createdBy = entity.getCreatedBy();
        this.expiryTime = entity.getExpiryTime();
        // Assuming entity.getCreationTimestamp() if you added it, otherwise use entity.getTimestamp() if RoomEntity stores Firebase's 'timestamp' field
        // Let's assume RoomEntity's timestamp field holds the creation timestamp for temporary rooms
        this.timestamp = entity.getTimestamp(); // Or entity.getCreationTimestamp() if named differently in Entity
        this.lastMessageText = entity.getLastMessageText();
        this.lastMessageTimestamp = entity.getLastMessageTimestamp(); // <-- Assigning Long to Long (Safe)
        this.hasUnreadMessages = entity.isHasUnreadMessages();
    }


    // --- Implement TemporaryIChatListItemGroup methods ---
    @Override
    public String getId() {
        return roomId;
    }

    @Override
    public String getName() {
        return roomName;
    }

    @Override
    public String getImageUrl() {
        // Temporary rooms might not have images, return null or a default temp room image
        return null; // Or a drawable resource ID if you have one for the adapter
    }

    @Override
    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    @Override
    public long getSortingTimestamp() {
        // IMPORTANT: Handle potential null values from Long fields
        // Sort primarily by last message timestamp.
        // If last message timestamp is null or 0, fallback to creation timestamp.
        // If creation timestamp is also null, fallback to 0.
        if (lastMessageTimestamp != null && lastMessageTimestamp > 0) {
            return lastMessageTimestamp;
        } else if (timestamp != null) {
            return timestamp; // Use creation timestamp
        } else {
            return 0L; // Fallback
        }
    }

    @Override
    public String getLastMessagePreview() {
        // Return the last message text for preview
        // You might want to truncate long messages here
        return lastMessageText != null ? lastMessageText : ""; // Return empty string if null
    }


    // --- Getters and Setters for TemporaryRoom specific fields ---
    public String getRoomId() { return roomId; } // Duplicate, but can keep for consistency
    public void setRoomId(String roomId) { this.roomId = roomId; } // Added setter

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; } // Added setter

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; } // Added setter

    public Long getExpiryTime() { return expiryTime; }
    public void setExpiryTime(Long expiryTime) { this.expiryTime = expiryTime; } // Added setter

    public Long getTimestamp() { return timestamp; } // Creation timestamp
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; } // Added setter

    public boolean isHasUnreadMessages() { return hasUnreadMessages; } // Added getter for consistency
    public void setUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }

    // Getters/Setters for last message fields (now using Long for timestamp)
    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }

    public Long getLastMessageTimestamp() { return lastMessageTimestamp; } // Returns Long
    public void setLastMessageTimestamp(Long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; } // Accepts Long


    // Method to check if expired (client-side check)
    public boolean isExpired() {
        return expiryTime != null && System.currentTimeMillis() > expiryTime;
    }
}