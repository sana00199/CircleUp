package com.sana.circleup;

import com.sana.circleup.room_db_implement.GroupEntity; // Import the Room Entity
import com.sana.circleup.temporary_chat_room.TemporaryIChatListItemGroup;


// Ensure this is in the correct package, likely com.sana.circleup.chat_list or similar
public class Group implements TemporaryIChatListItemGroup {
    private String groupId;
    private String groupName;
    private String groupStatus; // If you need this from Firebase, keep it. May not be in Entity.
    private boolean hasUnreadMessages;
    private String adminId; // --- NEW ---
    private boolean isCurrentUserMember; // --- NEW ---
    private String groupImage; // Base64 string

    private String lastMessageText;
    private Long lastMessageTimestamp; // <-- Changed from 'long' to 'Long'

    // Removed isCurrentUserMember as the presence in Room DB implies membership for that owner.

    // --- Constructors ---

    // Required for Firebase (empty constructor) - Firebase won't use this directly for list items
    public Group() {
    }

    // Constructor from Firebase data (modify to accept last message/timestamp) - Not used for list items
    // public Group(String groupId, String groupName, String groupStatus, String groupImage,
    //              String lastMessageText, long lastMessageTimestamp) {
    //     this.groupId = groupId;
    //     this.groupName = groupName;
    //     this.groupStatus = groupStatus;
    //     this.groupImage = groupImage;
    //     this.lastMessageText = lastMessageText;
    //     this.lastMessageTimestamp = lastMessageTimestamp; // This one accepts long, which is okay if you use it separately
    //     this.hasUnreadMessages = false; // Default, update separately
    // }

    // *** NEW Constructor to create model from Room Entity ***
    public Group(GroupEntity entity) {
        this.groupId = entity.getGroupId();
        this.groupName = entity.getGroupName();
        // this.groupStatus = entity.getGroupStatus(); // If groupStatus is in Entity, add it or handle
        this.groupImage = entity.getGroupImage();
        this.lastMessageText = entity.getLastMessageText();
        this.adminId = entity.getAdminId(); // --- NEW ---
        this.isCurrentUserMember = entity.isUserCurrentlyMember(); // -
        this.lastMessageTimestamp = entity.getLastMessageTimestamp(); // <-- Assigning Long to Long (Safe)
        this.hasUnreadMessages = entity.isHasUnreadMessages();
    }


    // --- Implement TemporaryIChatListItemGroup methods ---

    @Override
    public String getId() {
        return groupId;
    }

    @Override
    public String getName() {
        return groupName;
    }

    @Override
    public String getImageUrl() {
        return groupImage; // Returns Base64 string
    }



    public String getAdminId() { return adminId; } // --- NEW ---
    public boolean isCurrentUserMember() { return isCurrentUserMember; } // --- NEW ---
    public void setIsCurrentUserMember(boolean isCurrentUserMember) { this.isCurrentUserMember = isCurrentUserMember; } // --- NEW ---

    @Override
    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    @Override
    public long getSortingTimestamp() {
        // *** IMPORTANT: Handle potential null value from the Long field ***
        // Comparator.comparingLong requires a primitive long.
        return lastMessageTimestamp != null ? lastMessageTimestamp : 0L;
    }

    @Override
    public String getLastMessagePreview() { // Add this method to match the interface expectation for preview text
        // Use lastMessageText, potentially adding a type indicator if you have messageType in Entity
        // You might want to truncate long messages here for preview display
        return lastMessageText != null ? lastMessageText : ""; // Return empty string if null
    }


    // --- Getters and Setters ---

    public String getGroupId() {
        return groupId;
    }

    public void setUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupStatus() {
        return groupStatus;
    }

    public String getGroupImage() {
        return groupImage;
    }

    // Keep specific getter if needed elsewhere, it now returns Long
    public Long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    // Keep specific getter if needed elsewhere
    public String getLastMessageText() {
        return lastMessageText;
    }


    // Setters (adjust if needed, but constructor from Entity is primary source for list)
    public void setGroupId(String groupId) { this.groupId = groupId; } // Added setters for completeness
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setGroupImage(String groupImage) { this.groupImage = groupImage; }

    // Setter now accepts Long
    public void setLastMessageTimestamp(Long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }


    // Add setGroupStatus if you need to update it
    public void setGroupStatus(String groupStatus) { this.groupStatus = groupStatus; }
}