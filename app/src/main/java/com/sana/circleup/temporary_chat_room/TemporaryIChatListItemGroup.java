package com.sana.circleup.temporary_chat_room;

public interface TemporaryIChatListItemGroup {


    String getId(); // Returns groupId or roomId
    String getName(); // Returns groupName or roomName
    String getImageUrl(); // Returns groupImage (Base64) or maybe null for temp rooms initially
    boolean hasUnreadMessages(); // Returns unread status
    // Add other common methods needed for display or sorting, e.g.:
    long getSortingTimestamp(); // e.g., last message timestamp or creation timestamp

    String getLastMessagePreview();
    // String getLastMessagePreview(); // If you want to show a message preview

}
