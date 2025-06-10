package com.sana.circleup.room_db_implement;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

// Define a composite primary key for owner + conversation
@Entity(tableName = "wallpapers", primaryKeys = {"ownerUserId", "conversationId"})
public class WallpaperEntity {

    @NonNull
    @ColumnInfo(name = "ownerUserId")
    private String ownerUserId; // The UID of the user who set this wallpaper

    @NonNull
    @ColumnInfo(name = "conversationId")
    private String conversationId; // The ID of the chat conversation

    @ColumnInfo(name = "imageData", typeAffinity = ColumnInfo.BLOB)
    private byte[] imageData; // The image data as a byte array (BLOB)

    // Getters and Setters (Generate these)

    @NonNull
    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(@NonNull String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    @NonNull
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}