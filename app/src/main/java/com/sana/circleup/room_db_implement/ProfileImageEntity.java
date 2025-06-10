package com.sana.circleup.room_db_implement;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profile_images")
public class ProfileImageEntity {
    @PrimaryKey
    @NonNull
    private String userId;
    private String profileImage; // Base64 string

    public ProfileImageEntity(@NonNull String userId, String profileImage) {
        this.userId = userId;
        this.profileImage = profileImage;
    }

    public String getUserId() { return userId; }
    public String getProfileImage() { return profileImage; }
}
