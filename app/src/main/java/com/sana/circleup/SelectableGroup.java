package com.sana.circleup;

import android.text.TextUtils;


// Data model for a selectable permanent group in the recipient list
public class SelectableGroup implements SelectableRecipient {

    private String groupId;
    private String groupName;
    private String groupImage; // Base64 string

    private boolean isSelected = false; // Selection state

    // Type identifier for this recipient type
    private static final String RECIPIENT_TYPE = "group";


    // Constructor to create from Group model or Entity
    // Let's use the Group model which is built from the Entity for the list display
    public SelectableGroup(Group group) {
        if (group != null) {
            this.groupId = group.getId(); // Or group.getGroupId()
            this.groupName = group.getName(); // Or group.getGroupName()
            this.groupImage = group.getImageUrl(); // Or group.getGroupImage()
        } else {
            // Handle null Group gracefully
            this.groupId = null;
            this.groupName = "Invalid Group";
            this.groupImage = null;
        }
    }



    // --- Implement SelectableRecipient Interface Methods ---

    @Override
    public String getId() {
        return groupId;
    }

    @Override
    public String getName() {
        // Provide a fallback name if group name is empty
        return TextUtils.isEmpty(groupName) ? "Unnamed Group" : groupName;
    }

    @Override
    public String getImageUrl() {
        // Returns the Base64 string or URL
        return groupImage;
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
    public String getGroupName() { return groupName; }
    public String getGroupImage() { return groupImage; }

}