package com.sana.circleup;

public class Users {
    private String userId;
    private String username;
    private String email;
    private String status;
    private String role;
    private String profileImage;
    private boolean isBlocked;

    //  Default constructor required by Firebase
    public Users() {
    }

    //  Parameterized constructor
    public Users(String userId, String username, String email, String status, String role, String profileImage, boolean isBlocked) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.status = status;
        this.role = role;
        this.profileImage = profileImage;
        this.isBlocked = isBlocked;
    }

    //  Getters and Setters
    public String getUserId() {
        return userId;
    }

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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }


}
