package com.sana.circleup.room_db_implement;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfileEntity {

    @PrimaryKey
    @NonNull
    private String uid;


    private String username;
    private String email;
    private String profileImageBase64;

    // Constructors, getters & setters
    public UserProfileEntity(@NonNull String uid, String username, String email, String profileImageBase64) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.profileImageBase64 = profileImageBase64;
    }

    public UserProfileEntity()
    {

    }

    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getProfileImageBase64() { return profileImageBase64; }

    public void setUid(String uid) { this.uid = uid; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
}

