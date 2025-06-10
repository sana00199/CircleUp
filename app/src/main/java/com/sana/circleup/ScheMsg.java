package com.sana.circleup; // Replace with your actual package name

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//
//public class ScheMsg {
//    // --- Updated Fields ---
//    private String senderId;
//    private List<String> receiverIds; // List of receiver IDs
//    private List<String> receiverNames; // List of receiver names (for easier display/notification)
//    private String message; // The message content (plain text stored here)
//    private String scheduledTimeFormatted; // Formatted time string for display/storage
//    private String status; // e.g., "pending", "sent", "failed"
//    private String messageType; // e.g., "text", "image"
//    // private Map<String, String> receiverIdToNameMap; // Optional: map IDs to names directly
//
//
//    // --- Constructors ---
//    public ScheMsg() {} // Required for Firebase
//
//    // Main constructor for scheduling messages to multiple recipients
//    public ScheMsg(List<String> receiverIds, String senderId, List<String> receiverNames, String message, String scheduledTimeFormatted) {
//        this.senderId = senderId;
//        this.receiverIds = (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>(); // Use copies
//        this.receiverNames = (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>(); // Use copies
//        this.message = message;
//        this.scheduledTimeFormatted = scheduledTimeFormatted;
//        this.status = "pending"; // Default status for a new scheduled message
//        this.messageType = "text"; // Default type (update if you add image scheduling)
//    }
//
//    // You can keep previous constructors if they are used elsewhere, but the Worker uses the one above.
//    // Example of a previous constructor that might be confusing with multiple receivers:
//    // public ScheMsg(String senderId, String receiverId, String message, String scheduledTimeFormatted) { ... }
//    // -> Better to remove this if all scheduled messages are multi-recipient tasks now
//
//
//    // --- Getters and Setters (Ensure they match updated fields) ---
//    public String getSenderId() {
//        return senderId;
//    }
//
//    public List<String> getReceiverIds() {
//        // Return a copy to prevent external modification
//        return (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>();
//    }
//
//    public List<String> getReceiverNames() {
//        // Return a copy
//        return (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>();
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public String getScheduledTimeFormatted() {
//        return scheduledTimeFormatted;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getMessageType() {
//        return messageType;
//    }
//
//    public void setMessageType(String messageType) {
//        this.messageType = messageType;
//    }
//
//    // Optional: Getter for formatted receiver names string (for display)
//    public String getReceiverNamesString() {
//        return TextUtils.join(", ", getReceiverNames()); // Get a copy and join
//    }
//
//    // Optional: Getter for receiverIdToNameMap if you add it as a field
//    // public Map<String, String> getReceiverIdToNameMap() { return receiverIdToNameMap; }
//    // public void setReceiverIdToNameMap(Map<String, String> map) { this.receiverIdToNameMap = map; }
//
//    // The fields receiverId, receiverName, receiverNamesStr were likely for single-recipient or string display only.
//    // If they are not strictly needed for Firebase read/write or other parts of the app, consider removing them.
//    // Keeping receiverNamesStr might be useful for simpler display in the Drafts list adapter.
//    // Let's add back receiverNamesStr field and its getter/setter for adapter display convenience.
//
//    private String receiverNamesStr; // Added back for display convenience
//
//    public String getReceiverNamesStr() { return receiverNamesStr; }
//    public void setReceiverNamesStr(String receiverNamesStr) { this.receiverNamesStr = receiverNamesStr; }
//
//    // Update main constructor to set receiverNamesStr
//    public ScheMsg(String senderId, List<String> receiverIds, List<String> receiverNames, String message, String scheduledTimeFormatted) {
//        this.senderId = senderId;
//        this.receiverIds = (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>();
//        this.receiverNames = (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>();
//        this.message = message;
//        this.scheduledTimeFormatted = scheduledTimeFormatted;
//        this.status = "pending";
//        this.messageType = "text";
//        this.receiverNamesStr = TextUtils.join(", ", getReceiverNames()); // Set initially here
//    }
//
//    // If receiverIds/receiverNames are set individually after creation, remember to call setReceiverNamesStr
//    public void setReceiverIds(List<String> receiverIds) {
//        this.receiverIds = (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>();
//        // If names are not updated at the same time, receiverNamesStr might become inconsistent
//    }
//    public void setReceiverNames(List<String> receiverNames) {
//        this.receiverNames = (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>();
//        this.receiverNamesStr = TextUtils.join(", ", getReceiverNames()); // Update when names are set
//    }
//    // Ensure Firebase reads/writes handle these lists correctly.
//}




public class ScheMsg {
    // --- Updated Fields ---
    // This ID will be passed to the Worker
    private String msgFirebaseId;
    // The ID of the user who scheduled the message
    private String senderId;
    // List of IDs of the recipients
    private List<String> receiverIds;
    // List of names of the recipients (for easier display on sender's side drafts)
    private List<String> receiverNames;
    // Optional: Map of receiver IDs to names if needed
    private Map<String, String> receiverIdToNameMap;
    // The message content (TEXT or IMAGE BASE64), stored as is (NOT encrypted)
    private String content;
    // The type of message content ("text", "image")
    private String messageType;
    // The timestamp (in milliseconds) when the message should be sent
    private long scheduledTimeMillis;
    // A formatted string representation of the scheduled time (for display)
    private String scheduledTimeFormatted;
    // The current status of this scheduled entry ("pending", "processing", "sent", "failed", "cancelled")
    private String status;
    // A formatted string of receiver names (for display convenience)
    private String receiverNamesStr;


    private String message; // Added back for display convenience


    // --- Constructors ---
    public ScheMsg() {} // Required for Firebase

    // Constructor for scheduling messages
    // Use a builder pattern or separate methods for clarity if needed,
    // but for simplicity, let's update the constructor.
    // This constructor assumes 'content' is EITHER text OR image Base64 based on 'messageType'
    public ScheMsg(String senderId, List<String> receiverIds, List<String> receiverNames,
                   String content, String messageType,
                   long scheduledTimeMillis, String scheduledTimeFormatted) {
        this.senderId = senderId;
        // Store copies of the lists to avoid external modification issues
        this.receiverIds = (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>();
        this.receiverNames = (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>();
        this.content = content; // Store content as is
        this.messageType = (messageType != null && !messageType.isEmpty()) ? messageType : "text"; // Set the type, default to text
        this.scheduledTimeMillis = scheduledTimeMillis;
        this.scheduledTimeFormatted = scheduledTimeFormatted;
        this.status = "pending"; // Default status

        // Populate receiverIdToNameMap if names list is not null
        this.receiverIdToNameMap = new HashMap<>();
        if(receiverIds != null && receiverNames != null && receiverIds.size() == receiverNames.size()){
            for(int i=0; i<receiverIds.size(); i++){
                this.receiverIdToNameMap.put(receiverIds.get(i), receiverNames.get(i));
            }
        } else {
            // Log a warning or handle if lists don't match
            android.util.Log.w("ScheMsg", "Receiver IDs and Names lists size mismatch or null during constructor. Map may be incomplete.");
            // Attempt to populate map from IDs if names are missing (requires fetching elsewhere)
            // For now, map might be empty or incomplete
        }


        // Generate the display string for receiver names
        this.receiverNamesStr = TextUtils.join(", ", this.receiverNames); // Join the stored names
    }


    public String getMsgFirebaseId() {
        return msgFirebaseId;
    }

    public void setMsgFirebaseId(String msgFirebaseId) {
        this.msgFirebaseId = msgFirebaseId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public List<String> getReceiverIds() {
        // Return a copy to prevent external modification
        return (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>();
    }

    public void setReceiverIds(List<String> receiverIds) {
        this.receiverIds = (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>();
        // Note: Updating IDs separately requires updating names and namesStr manually
    }

    public List<String> getReceiverNames() {
        // Return a copy
        return (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>();
    }

    public void setReceiverNames(List<String> receiverNames) {
        this.receiverNames = (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>();
        this.receiverNamesStr = TextUtils.join(", ", this.receiverNames); // Update names string
    }

    public Map<String, String> getReceiverIdToNameMap() {
        return (receiverIdToNameMap != null) ? new HashMap<>(receiverIdToNameMap) : new HashMap<>(); // Return copy
    }

    public void setReceiverIdToNameMap(Map<String, String> receiverIdToNameMap) {
        this.receiverIdToNameMap = (receiverIdToNameMap != null) ? new HashMap<>(receiverIdToNameMap) : new HashMap<>();
    }


    public String getContent() {
        return content; // Return content as is (plaintext or Base64)
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = (messageType != null && !messageType.isEmpty()) ? messageType : "text";
    }

    public long getScheduledTimeMillis() {
        return scheduledTimeMillis;
    }

    public void setScheduledTimeMillis(long scheduledTimeMillis) {
        this.scheduledTimeMillis = scheduledTimeMillis;
    }

    public String getScheduledTimeFormatted() {
        return scheduledTimeFormatted;
    }

    public void setScheduledTimeFormatted(String scheduledTimeFormatted) {
        this.scheduledTimeFormatted = scheduledTimeFormatted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiverNamesStr() {
        return receiverNamesStr;
    }

    public void setReceiverNamesStr(String receiverNamesStr) {
        this.receiverNamesStr = receiverNamesStr;
    }


    // --- Helper Getters for Adapter Display (Optional) ---
    // These might be useful for the Drafts list adapter

    public String getReceiverInfoForDisplay() {
        return "To: " + getReceiverNamesStr(); // Use the joined names string
    }

    public String getContentPreviewForDisplay() {
        // For this non-E2EE scheduled feature, we can display the full content or a placeholder
        if ("text".equals(messageType)) {
            // Show full text content
            return !TextUtils.isEmpty(content) ? content : "[Empty Text Message]";
        } else if ("image".equals(messageType)) {
            return "[Image]"; // Show placeholder for image in the list preview
        } else {
            return "[" + messageType + "]"; // Fallback for other types
        }
    }



    public String getMessage() {
        return message;
    }

    // Optional: Getter for receiverIdToNameMap if you add it as a field
    // public Map<String, String> getReceiverIdToNameMap() { return receiverIdToNameMap; }
    // public void setReceiverIdToNameMap(Map<String, String> map) { this.receiverIdToNameMap = map; }

}