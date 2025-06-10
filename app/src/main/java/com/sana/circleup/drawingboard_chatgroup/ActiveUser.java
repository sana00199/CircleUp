package com.sana.circleup.drawingboard_chatgroup;



import androidx.annotation.Nullable; // Import for @Nullable

// Simple model class to represent an active user in the drawing session
public class ActiveUser {
    private String userId; // Will store the Firebase UID
    private String name;   // Will store the username
    @Nullable
    private String profileImageBase64; // Will store the Base64 profile image

    // Default constructor required for Firebase deserialization if needed (though we mostly build it manually)
    public ActiveUser() {}

    // Constructor to create an ActiveUser object
    public ActiveUser(String userId, String name, @Nullable String profileImageBase64) {
        this.userId = userId;
        this.name = name;
        this.profileImageBase64 = profileImageBase64;
    }

    // Getters (Needed to access the data)
    public String getUserId() { return userId; }
    public String getName() { return name; }
    @Nullable
    public String getProfileImageBase64() { return profileImageBase64; }

    // Setters (Optional, but good practice)
    public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setProfileImageBase64(@Nullable String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
}