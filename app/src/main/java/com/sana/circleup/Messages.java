package com.sana.circleup;

public class Messages {
    private String message, type, from, encryptedAESKey, sendTime, seenTime;
    private String messageId;
    private String senderProfile, receiverProfile;
    private String fileName, fileType, fileUrl;
    private boolean seen; //  Added missing 'seen' variable
    private String status; // Add status field (sent, pending, etc.)
    private long timestamp;

    public Messages(String base64Image, String image, String messageSenderID, String messageReceiverID, boolean b, String sendTime) {
        // Required empty constructor for Firebase
    }


    // No-argument constructor for Firebase
    public Messages() {
        // Firebase requires this constructor to deserialize objects from the database
    }

    public Messages(String messageId, String from, String message, String type, String sendTime, boolean seen, String seenTime, String status) {
        this.messageId = messageId;
        this.from = from;
        this.message = message;
        this.type = type;
        this.sendTime = sendTime;
        this.seen = seen;
        this.seenTime = seenTime;
        this.status = status;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMessageId() {
        return messageId;
    }

    public String getEncryptedAESKey() { return encryptedAESKey; }
    public void setEncryptedAESKey(String encryptedAESKey) { this.encryptedAESKey = encryptedAESKey; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getSenderProfile() { return senderProfile; }
    public void setSenderProfile(String senderProfile) { this.senderProfile = senderProfile; }

    public String getReceiverProfile() { return receiverProfile; }
    public void setReceiverProfile(String receiverProfile) { this.receiverProfile = receiverProfile; }

    public String getSendTime() { return sendTime; }
    public void setSendTime(String sendTime) { this.sendTime = sendTime; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public String getSeenTime() { return seenTime; }
    public void setSeenTime(String seenTime) { this.seenTime = seenTime; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    // Getters for status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Assuming you store a field for the receiver somewhere, or you can derive it
    // If not, you can always assume it's the other user
    public String getReceiverId(String currentUserId) {
        return from.equals(currentUserId) ? "otherUserId" : currentUserId;
    }

    public String getId() {
        return messageId;
    }

    public String getReceiver() {
        return from.equals("senderId") ? "receiverId" : "senderId";
    }

    public long getTimestamp() {
        return timestamp;

    }

    // Add the missing setter for timestamp (optional, but good practice)
    public void setTimestamp(long timestamp) { // <<< Add this method
        this.timestamp = timestamp;
    }
}
