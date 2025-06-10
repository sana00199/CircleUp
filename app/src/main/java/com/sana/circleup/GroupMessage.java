package com.sana.circleup;





import com.google.firebase.database.ServerValue; // Import ServerValue

import java.util.HashMap;
import java.util.Map; // Import Map

public class GroupMessage {
    // Existing fields
    private String date, message, name, time, senderId;
    private String imageBase64;
    private String type;

    // --- NEW Fields for Firebase Mapping and Features ---
    private Long timestamp; // To store the server timestamp
    private Map<String, Boolean> readBy; // To store which users have read this message (UID -> true)

    // --- Field for Client-side Unread Status (Optional, can calculate from readBy) ---
    // private transient boolean unread; // transient keyword means it won't be serialized/deserialized by Firebase


    public GroupMessage() {
        // Default constructor required for Firebase
    }

    // You might need constructors that match the data you store in Firebase messages node
    // For Firebase to map automatically, ensure field names match keys OR use default constructor + setters

    // Example Constructor matching your Firebase structure (simplified)
    // Note: If Firebase keys don't exactly match these names, use default constructor and setters.
    public GroupMessage(String date, String message, String name, String time, String senderId, String type,
                        String imageBase64, long timestamp, Map<String, Boolean> readBy) {
        this.date = date;
        this.message = message;
        this.name = name;
        this.time = time;
        this.senderId = senderId;
        this.type = type;
        this.imageBase64 = imageBase64; // Will be null for text messages
        this.timestamp = timestamp;
        this.readBy = readBy;
        // unread status can be calculated
    }


    // --- Existing Getters and Setters ---
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }


    // --- NEW Getters and Setters for Firebase Mapping ---
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; } // Use this setter if not using ServerValue.TIMESTAMP directly

    public Map<String, Boolean> getReadBy() { return readBy; }
    public void setReadBy(Map<String, Boolean> readBy) { this.readBy = readBy; }

    // --- Method to check read status client-side ---
    public boolean isReadBy(String userId) {
        return readBy != null && readBy.containsKey(userId) && Boolean.TRUE.equals(readBy.get(userId));
    }

    // --- Method to mark message as read by a user ---
    public void markAsReadBy(String userId) {
        if (readBy == null) {
            readBy = new HashMap<>();
        }
        readBy.put(userId, true);
        // Note: This only updates the local object. You need to update Firebase separately.
    }


}


