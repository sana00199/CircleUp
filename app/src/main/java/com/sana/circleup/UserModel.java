package com.sana.circleup;

public class UserModel {
    private String userId;
    private String username;
    private String email;
    private String status;
    private String role;
    private boolean isBlocked;
    private String profileImage;


    // 🔹 Default (no-argument) constructor required for Firebase
    public UserModel(String friendUid) {
    }
    public UserModel() {
        // This constructor is needed by Firebase
    }

    public UserModel(String userId, String username, String profileImage) {
        this.userId = userId;
        this.username = username;
        this.profileImage = profileImage;
    }


    public UserModel(String userId, String username, String email, String status, String role, boolean isBlocked, String profileImage) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.status = status;
        this.role = role;
        this.isBlocked = isBlocked;
        this.profileImage = profileImage;
    }


    // Setter for userId (You already have this)
    public void setUserId(String userId) {
        this.userId = userId;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUserId() { return userId; }


}


