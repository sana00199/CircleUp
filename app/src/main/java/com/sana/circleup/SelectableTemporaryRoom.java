package com.sana.circleup;

import android.text.TextUtils;

import com.sana.circleup.temporary_chat_room.TemporaryRoom;

// Data model for a selectable temporary room in the recipient list
public class SelectableTemporaryRoom implements SelectableRecipient {

    private String roomId;
    private String roomName;
    private Long expiryTime; // To check if expired (though typically expired rooms wouldn't be listed)

    private boolean isSelected = false; // Selection state

    // Type identifier for this recipient type
    private static final String RECIPIENT_TYPE = "temporary_room";


    // Constructor to create from TemporaryRoom model
    public SelectableTemporaryRoom(TemporaryRoom temporaryRoom) {
        if (temporaryRoom != null) {
            this.roomId = temporaryRoom.getId(); // Or temporaryRoom.getRoomId()
            this.roomName = temporaryRoom.getName(); // Or temporaryRoom.getRoomName()
            this.expiryTime = temporaryRoom.getExpiryTime();
        } else {
            // Handle null TemporaryRoom gracefully
            this.roomId = null;
            this.roomName = "Invalid Room";
            this.expiryTime = null;
        }
    }

    // --- Implement SelectableRecipient Interface Methods ---

    @Override
    public String getId() {
        return roomId;
    }

    @Override
    public String getName() {
        // Provide a fallback name if room name is empty
        return TextUtils.isEmpty(roomName) ? "Unnamed Room" : roomName;
    }

    @Override
    public String getImageUrl() {
        // Temporary rooms usually don't have images, return null
        return null;
    }

    @Override
    public String getType() {
        return RECIPIENT_TYPE;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    // --- Optional: Add other getters if needed ---
    public String getRoomName() { return roomName; }
    public Long getExpiryTime() { return expiryTime; }

    // Helper to check if the room is expired (optional, might filter expired rooms before displaying)
    public boolean isExpired() {
        return expiryTime != null && System.currentTimeMillis() > expiryTime;
    }
}