package com.sana.circleup.room_db_implement;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Represents a user profile from the /Users node
@Entity(tableName = "users") // Table name for all users in Find Friends context
public class UserEntity {

    // The Firebase UID is the primary key
    @PrimaryKey
    @NonNull
    private String userId; // Corresponds to the key under /Users/{userId}
    private String encryptedPrivateKeyBase64; // *** NEW FIELD: Store user's encrypted private key Base64 ***

    private String username;
    private String role; // "User", "Admin" etc.
    private boolean isBlocked; // For admin panel/reporting
    private String status;
    private String profileImage; // Assuming this stores the Base64 string
    private String email; // Adding email as per your screenshot

    // You can add other fields from your /Users node here if needed
    // For example, role, isBlocked, etc.
    // Note: Real-time status like 'online' (userState) is harder to sync frequently into Room for ALL users.
    // If you need real-time online status, consider handling it via Firebase listeners attached to visible items or a separate sync mechanism.
    // For this version, we include basic profile fields.


    // Constructor required by Room (includes all fields marked as columns)
    public UserEntity(@NonNull String userId, String username, String status, String profileImage, String email) {
        this.userId = userId;
        this.username = username;
        this.status = status;
        this.profileImage = profileImage;
        this.email = email;
    }


    // Constructor required by Room (includes fields that map to columns)
//    public UserEntity(@NonNull String userId, String username, String status, String profileImage, String email, String role, boolean isBlocked, String encryptedPrivateKeyBase64) {
//        this.userId = userId;
//        this.username = username;
//        this.status = status;
//        this.profileImage = profileImage;
//        this.email = email;
//        this.role = role;
//        this.isBlocked = isBlocked;
//        this.encryptedPrivateKeyBase64 = encryptedPrivateKeyBase64; // *** Initialize NEW field ***
//    }

    // Room often requires an empty constructor, even if you provide a full one
    public UserEntity() {
    }

    // --- Getters (Needed by Room and for accessing data) ---
    @NonNull
    public String getUserId() { return userId; }

    public String getUsername() { return username; }

    public String getStatus() { return status; }

    public String getProfileImage() { return profileImage; } // Make sure this name matches your Firebase field name
    public String getEmail() { return email; } // Make sure this name matches your Firebase field name

    public String getRole() { return role; }
    public boolean isBlocked() { return isBlocked; }
    public String getEncryptedPrivateKeyBase64() { return encryptedPrivateKeyBase64; } // *** NEW Getter ***

    // --- Setters (Often needed by Room for population/updates) ---
    // Room can use setters to populate objects if an empty constructor is present
    public void setUserId(@NonNull String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setStatus(String status) { this.status = status; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setEmail(String email) { this.email = email; }

    public void setRole(String role) { this.role = role; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }
    public void setEncryptedPrivateKeyBase64(String encryptedPrivateKeyBase64) { this.encryptedPrivateKeyBase64 = encryptedPrivateKeyBase64; } // *** NEW Setter ***


    // You might add setters for other fields if you add them
}

//@Entity(tableName = "users")
//public class UserEntity {
//    @PrimaryKey
//    @NonNull
//    public String userId;
//
//    public String username;
//    public String email;
//    public String profileImage;
//    public String status;
//    public boolean isBlocked;
//    public String lastSeen;
//
//    public UserEntity() {
//        userId = "";
//    }
//}

