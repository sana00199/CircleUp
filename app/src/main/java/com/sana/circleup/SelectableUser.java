package com.sana.circleup;

import android.text.TextUtils;

// Data model for a selectable user contact in the recipient list
public class SelectableUser implements SelectableRecipient {

    private String userId;
    private String username;
    private String profileImage; // Base64 string or URL

    private boolean isSelected = false; // Selection state

    // Type identifier for this recipient type
    private static final String RECIPIENT_TYPE = "user";

    // Constructor to create from UserModel
    public SelectableUser(UserModel userModel) {
        if (userModel != null) {
            this.userId = userModel.getUserId();
            this.username = userModel.getUsername();
            this.profileImage = userModel.getProfileImage(); // Assuming profileImage is Base64/URL
        } else {
            // Handle null UserModel gracefully if possible, or log error
            this.userId = null; // Or a placeholder ID
            this.username = "Invalid User"; // Placeholder name
            this.profileImage = null;
        }
    }



    // --- Implement SelectableRecipient Interface Methods ---

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public String getName() {
        // Provide a fallback name if username is empty
        return TextUtils.isEmpty(username) ? "Unknown User" : username;
    }

    @Override
    public String getImageUrl() {
        // Returns the Base64 string or URL
        return profileImage;
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

    // --- Optional: Add other getters if needed for adapter ---
    public String getUsername() { return username; }
    public String getProfileImage() { return profileImage; }
}