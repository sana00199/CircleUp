package com.sana.circleup.room_db_implement;


import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "deleted_msg_for_me", primaryKeys = {"ownerUserId", "firebaseMessageId"})
public class DeletedMessageIdEntity {

    @NonNull
    public String ownerUserId; // The ID of the user who deleted the message locally

    @NonNull
    public String firebaseMessageId; // The Firebase ID of the message

    // Constructor
    public DeletedMessageIdEntity(@NonNull String ownerUserId, @NonNull String firebaseMessageId) {
        this.ownerUserId = ownerUserId;
        this.firebaseMessageId = firebaseMessageId;
    }

    // Getters (optional but good practice if fields are private)
    @NonNull
    public String getOwnerUserId() {
        return ownerUserId;
    }

    @NonNull
    public String getFirebaseMessageId() {
        return firebaseMessageId;
    }
}