package com.sana.circleup.room_db_implement;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Using a composite primary key (groupId, ownerUserId)
@Entity(tableName = "groupss",
        primaryKeys = {"groupId", "owner_user_id"}) // Define composite primary key
public class GroupEntity {

    @NonNull
    public String groupId; // The ID of the group from Firebase

    // The ID of the user who is logged in and sees this group in their list
    @ColumnInfo(name = "owner_user_id", index = true) // Index for faster lookups
    @NonNull
    public String ownerUserId;
    // --- NEW Fields ---
    private String adminId; // The UID of the group administrator
    private boolean isUserCurrentlyMember; // True if the ownerUserId is currently a member in Firebase

    // Data needed for the list display
    public String groupName;
    public String groupImage; // Base64 string or URL
    public String lastMessageText; // Latest message content preview
    public Long lastMessageTimestamp; // Timestamp of the latest message for sorting

    // Flag to indicate if the current user has unread messages in this group
    // Note: Managing unread count accurately in group chats can be complex.
    // A simple boolean might be enough for display, or you might store the count.
    // Let's store a boolean for simplicity initially.
    public boolean hasUnreadMessages;

    // You might still want to store some other group-level data if needed, e.g., adminId
    // public String adminId; // Keep or remove based on need in the list view

    // Room requires a no-argument constructor if you define others
    public GroupEntity() {}

    // Add getters and setters for all fields

    @NonNull
    public String getGroupId() { return groupId; }
    public void setGroupId(@NonNull String groupId) { this.groupId = groupId; }

    @NonNull
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(@NonNull String ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupImage() { return groupImage; }
    public void setGroupImage(String groupImage) { this.groupImage = groupImage; }

    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }

    public Long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(Long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }

    public boolean isHasUnreadMessages() { return hasUnreadMessages; }
    public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }

    // If you kept adminId:
    // public String getAdminId() { return adminId; }
    // public void setAdminId(String adminId) { this.adminId = adminId; }


    // --- NEW Getters and Setters ---
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public boolean isUserCurrentlyMember() { return isUserCurrentlyMember; } // Keep getter name consistent
    public void setUserCurrentlyMember(boolean userCurrentlyMember) { isUserCurrentlyMember = userCurrentlyMember; }
    // --- End NEW Fields -


}
