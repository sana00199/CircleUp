package com.sana.circleup;

// Interface to represent any item that can be selected as a scheduled message recipient (User, Group, Temporary Room)
public interface SelectableRecipient {

    // Unique ID of the recipient (User UID, Group ID, Temporary Room ID)
    String getId();

    // Display name of the recipient (Username, Group Name, Room Name)
    String getName();

    // Optional: Image URL or Base64 for display
    String getImageUrl(); // Can be null or empty for some types

    // Type of recipient (e.g., "user", "group", "temporary_room") - Crucial for handling in the worker
    String getType();

    // Boolean state to track if the item is currently selected in the list
    boolean isSelected();

    // Method to set the selection state
    void setSelected(boolean selected);

    // You could add other relevant methods if needed for display or sorting (e.g., getLastMessagePreview, getSortingTimestamp - maybe not needed for selection list)
}