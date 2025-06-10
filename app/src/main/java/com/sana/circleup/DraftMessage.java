package com.sana.circleup;

import java.util.List;

public class DraftMessage {
    private String userId;
    private String message;
    private String scheduledDateTime;
    private List<String> contactIds;

    // No-argument constructor required by Firebase
    public DraftMessage() {
        // Firebase requires this constructor to work properly.
    }

    // Constructor for easy initialization
    public DraftMessage(String userId, String message, String scheduledDateTime, List<String> contactIds) {
        this.userId = userId;
        this.message = message;
        this.scheduledDateTime = scheduledDateTime;
        this.contactIds = contactIds;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public String getScheduledDateTime() {
        return scheduledDateTime;
    }

    public List<String> getContactIds() {
        return contactIds;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setScheduledDateTime(String scheduledDateTime) {
        this.scheduledDateTime = scheduledDateTime;
    }

    public void setContactIds(List<String> contactIds) {
        this.contactIds = contactIds;
    }
}


