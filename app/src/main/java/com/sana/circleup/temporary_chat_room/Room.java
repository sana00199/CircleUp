package com.sana.circleup.temporary_chat_room;

public class Room {
    private String id;
    private String name;
    private String type; // "TemporaryRoom"
    private String createdBy;
    private long expiryTime; // timestamp in millis

    public Room(String roomId, String roomName, String temporaryRoom) {
        // Required for Firebase
    }

    public Room(String id, String name, String type, String createdBy, long expiryTime) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.createdBy = createdBy;
        this.expiryTime = expiryTime;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }
}
